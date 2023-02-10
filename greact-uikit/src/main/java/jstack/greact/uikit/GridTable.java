package jstack.greact.uikit;

import jstack.greact.dom.HTMLNativeElements.*;
import jstack.jscripter.transpiler.model.Require;
import jstack.jscripter.transpiler.model.JSExpression;
import jstack.greact.dom.GReact;
import jstack.greact.dom.HTMLElement;

import java.util.function.BiFunction;
import java.util.function.Consumer;

@Require.CSS("grid.css") class GridTable<T> implements Component0<table> {
    static class RowData<T> {
        final T data;
        boolean expanded = false;
        boolean editing = false;
        RowData(T data) { this.data = data; }
    }

    final GridConfig2<T> conf;
    final Runnable onFilterEnableDisable;
    final Consumer<T> onRowSelect;

    boolean addNewRowMode = false;
    RowData<T>[] rows;
    RowData<T> selectedRow = null;
    HTMLElement theTable;
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
        this.rows = Array.map(data, RowData::new);
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
            id = "grid-table";

            new thead() {{
                new tr() {{
                    for (var colWithSize : Array.zip(Array.filter(conf.columns, c -> !c.hidden), columnSizes))
                        new td() {{
                            if (colWithSize.b != 0)
                                style.width = colWithSize.b + "px";
                            new span(colWithSize.a.header);
                        }};
                    new td() {{
                        className = "grid-table-td";
                        new div() {{
                            id = "grid-table-td-body";
                            className = "toolbox-header";
                            new div() {{
                                className = "grid-table-td-content";
                                innerHTML = """
                                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="feather feather-filter" style="margin-top: 4px;"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon></svg>
                                    """;
                                onclick = ev -> onFilterEnableDisable.run();
                            }};

                            if (conf.onRowAdd != null)
                                new div() {{
                                    className = "grid-table-td-content";
                                    innerHTML = """
                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="feather feather-plus" style="margin-top: 4px;"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
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
                            className = "grid-table-tbody-tr";
                            if ((conf.selectedRow != null && selectedRow == row) || row.expanded)
                                id = "grid-table-tbody-tr-background";

                            onclick = ev -> {
                                effect(selectedRow = row);
                                onRowSelect.accept(selectedRow.data);
                            };

                            for (var col : conf.columns)
                                if (!col.hidden) {
                                    var colValue = Grid.fetchValue(row.data, col.memberNames);
                                    GReact.mmountWith(this, col.view, nativeEl -> {
                                        var bgColor = col.backgroundColor;
                                        if (bgColor != null)
                                            nativeEl.style.backgroundColor = ((BiFunction<Object, T, String>) bgColor).apply(colValue, row.data);
                                    }, colValue, row.data);
                                }

                            new td() {{ /* toolbox */
                                className = "grid-table-toolbox";
                                new div() {{
                                    id = "grid-table-toolbox-body";
                                    className = "toolbox";

                                    if (conf.onRowDelete != null)
                                        new div() {{ /* delete */
                                            innerHTML = """
                                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="feather feather-trash" style="margin-top: 8px;"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                                                """;
                                            onclick = ev -> {
                                                ev.stopPropagation();
                                                if (JSExpression.of("window.confirm('Действительно удалить?')")) {
                                                    conf.onRowDelete.handle(row.data);
                                                    effect(rows = Array.filter(rows, r -> r != row));
                                                }
                                            };
                                        }};
                                    if (conf.onRowChange != null)
                                        new div() {{ /* edit */
                                            innerHTML = """
                                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="feather feather-edit" style="margin-top: 8px;"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
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
                                    if (conf.expandedRow != null)
                                        new div() {{ /* expand */
                                            innerHTML = row.expanded
                                                ? """
                                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="feather feather-chevron-up" style="margin-top: 8px;"><polyline points="18 15 12 9 6 15"/></svg>
                                                """
                                                : """
                                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="feather feather-chevron-down" style="margin-top: 8px;"><polyline points="6 9 12 15 18 9"/></svg>
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
                            id = "grid-table-expansion-f";
//                            if (selectedRow == row) style.backgroundColor = "#acea9f";
                            new td() {{
                                colSpan = conf.columns.length + 1;
                                className = "grid-table-expansion-td";

                                new div() {{
                                    className = "grid-table-expansion-td-body";
                                    new div() {{
                                        className = "grid-table-expansion-td-content";
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