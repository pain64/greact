package com.over64.greact.uikit;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.dom.HtmlElement;

import java.util.function.Consumer;

class GridTable<T> implements Component0<table> {
    static class RowData<T> {
        final T data;
        boolean expanded = false;
        boolean editing = false;
        RowData(T data) {this.data = data;}
    }

    final GridConfig2<T> conf;
    final Runnable onFilterEnableDisable;
    final Consumer<T> onRowSelect;

    boolean addNewRowMode = false;
    RowData<T>[] rows;
    RowData<T> selectedRow = null;
    HtmlElement theTable;
    Integer[] columnSizes;

    void keepSizes() {
        columnSizes = JSExpression.of("[].slice.call(this.theTable.tHead.rows[0].cells).map(e => e" +
            ".getBoundingClientRect().width)");
    }
    void clearSizes() {
        columnSizes = Array.map(conf.columns, v -> 0);
    }

    GridTable(T[] data, GridConfig2<T> conf,
              Consumer<T> onRowSelect, Runnable onFilterEnableDisable) {
        this.rows = Array.map(data, d -> new RowData<>(d)); /* FIXME: support method reference to new */
        this.conf = conf;
        this.onRowSelect = onRowSelect;
        this.onFilterEnableDisable = onFilterEnableDisable;
    }

    @Override public table mount() {
        clearSizes();

        if (rows.length != 0 && conf.selectedRow != null) {
            onRowSelect.accept(rows[0].data);
            selectedRow = rows[0];
        }

        return new table() {{
            theTable = this;

            className = "table table-striped";
            style.margin = "0px 0px 0px 0px";

            new thead() {{
                new tr() {{
                    for (var colWithSize : Array.zip(conf.columns, columnSizes))
                        new td() {{
                            if (colWithSize.b != 0)
                                style.width = colWithSize.b + "px";
                            new span(colWithSize.a._header);
                        }};
                    new td() {{
                        style.display = "flex";
                        style.justifyContent = "flex-end";
                        new div() {{
                            style.width = "54px";
                            style.display = "flex";
                            style.justifyContent = "flex-end";
                            className = "toolbox-header";
                            new div() {{
                                style.width = "16px";
                                innerHTML = """                           
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-filter"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon></svg>
                                    """;
                                onclick = ev -> onFilterEnableDisable.run();
                            }};
                            new div() {{
                                style.width = "16px";
                                innerHTML = """
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-plus"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                                    """;
                                onclick = ev -> {
                                    keepSizes();
                                    effect(addNewRowMode = true);
                                    effect(columnSizes);
                                    clearSizes();
                                };
                            }};
                        }};
                    }};
                }};
            }};
            new tbody() {{
                if (addNewRowMode)
                    new GridRowAdd<>(conf,
                        newRowData -> {
                            var persisted = conf.onRowAdd.supply(newRowData);
                            var newRow = new RowData<>(persisted);
                            // add row locally
                            // add row to filtered data
                            // add row to grid data
                            JSExpression.of("this.rows.splice(0, 0, newRow)");
                            effect(rows);
                            effect(addNewRowMode = false);
                        },
                        () -> effect(addNewRowMode = false));

                for (var row : rows) {
                    if (row.editing)
                        new GridRowEdit<>(row.data, conf, () -> {
                            row.editing = false;
                            effect(row);
                        });
                    else
                        new tr() {{
                            style.cursor = "pointer";
                            if (selectedRow == row || row.expanded)
                                style.backgroundColor = "#acea9f";

                            onclick = ev -> {
                                effect(selectedRow = row);
                                onRowSelect.accept(selectedRow.data);
                            };


                            for (var col : conf.columns)
                                new slot<>(
                                    (Component1<td, Object>) col._view,
                                    Grid.fetchValue(row.data, col.memberNames));

                            new td() {{ /* toolbox */
                                style.display = "flex";
                                style.justifyContent = "flex-end";
                                new div() {{
                                    style.width = "54px";
                                    style.display = "flex";
                                    className = "toolbox";

                                    new div() {{ /* delete */
                                        innerHTML = """
                                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-trash"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                                            """;
                                        onclick = ev -> {
                                            ev.stopPropagation();
                                            conf.onRowDelete.handle(row.data);
                                            effect(rows = Array.filter(rows, r -> r != row));
                                        };
                                    }};
                                    new div() {{ /* edit */
                                        innerHTML = """
                                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-edit"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                                            """;
                                        onclick = ev -> {
                                            ev.stopPropagation();
                                            row.expanded = false;
                                            row.editing = true;
                                            keepSizes();
                                            effect(columnSizes);
                                            effect(row);
                                            clearSizes();
                                        };
                                    }};
                                    new div() {{ /* expand */
                                        innerHTML = row.expanded
                                            ? """
                                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-minimize-2"><polyline points="4 14 10 14 10 20"/><polyline points="20 10 14 10 14 4"/><line x1="14" y1="10" x2="21" y2="3"/><line x1="3" y1="21" x2="10" y2="14"/></svg>
                                            """
                                            : """
                                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-maximize-2"><polyline points="15 3 21 3 21 9"/><polyline points="9 21 3 21 3 15"/><line x1="21" y1="3" x2="14" y2="10"/><line x1="3" y1="21" x2="10" y2="14"/></svg>
                                            """;
                                        onclick = ev -> {
                                            ev.stopPropagation();
                                            row.expanded = !row.expanded;
                                            effect(row);
                                        };
                                    }};

                                }};
                            }};
                        }};

                    if (row.expanded) {
                        new tr(); // fake for stripe color save
                        new tr() {{
                            className = "expansion-row";
                            style.backgroundColor = "#acea9f";
                            if (selectedRow == row) style.backgroundColor = "#acea9f";
                            new td() {{
                                colSpan = conf.columns.length + 1;
                                style.padding = "10px";
                                style.borderBottom = "1px solid white";
                                new div() {{
                                    style.backgroundColor = "white";
                                    style.display = "flex";
                                    style.justifyContent = "center";
                                    new div() {{
                                        style.width = "100%";
                                        new slot<>(conf.expandedRow, row.data);
                                    }};
                                }};
                            }};
                        }};
                    }
                }
            }};
        }};
    }
}