package jstack.greact.uikit;

import jstack.greact.html.*;
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
    final boolean filterEnabled;
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

    GridTable(
        T[] data, GridConfig2<T> conf, boolean filterEnabled,
        Consumer<T> onRowSelect, Runnable onFilterEnableDisable
    ) {
        this.filterEnabled = filterEnabled;
        this.rows = Array.map(data, RowData::new);
        this.conf = conf;
        this.onRowSelect = onRowSelect;
        this.onFilterEnableDisable = onFilterEnableDisable;
    }

    @Override public table render() {
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
                                new div("grid-i-filter");
                                className = filterEnabled ? "enabled" : "";
                                onclick = ev -> onFilterEnableDisable.run();
                            }};

                            if (conf.onRowAdd != null)
                                new div() {{
                                    new div("grid-i-plus");
                                    onclick = ev -> {
                                        keepSizes();
                                        effect(columnSizes, addNewRowMode = true);
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
                            JSExpression.of("this.rows.splice(0, 0, :1)", newRow);
                            effect(rows, addNewRowMode = false);
                        },
                        () -> {
                            effect(addNewRowMode = false);
                        });

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
                                    GReact.mountWith(this, col.view, nativeEl -> {
                                        var bgColor = col.backgroundColor;
                                        if (bgColor != null)
                                            nativeEl.style.backgroundColor = ((BiFunction<Object, T, String>) bgColor).apply(colValue, row.data);
                                    }, colValue, row.data);
                                }

                            new td() {{ /* toolbox */
                                className = "toolbox";
                                new slot<>(conf.rowActions.supply(null, row.data));
//                                new div() {{
//                                    id = "grid-table-toolbox-body";
//                                    className = "toolbox";
//
//                                    new slot<>(conf.rowActions.supply(null, row.data));

//                                    if (conf.onRowChange != null)
//                                        new div() {{ /* edit */
//                                            new div("grid-i-edit");
//                                            onclick = ev -> {
//                                                ev.stopPropagation();
//                                                row.expanded = false;
//                                                row.editing = true;
//                                                keepSizes();
//                                                effect(columnSizes);
//                                                effect(row);
//                                                clearSizes();
//                                            };
//                                        }};
//                                    if (conf.onRowDelete != null)
//                                        new div() {{ /* delete */
//                                            new div("grid-i-trash");
//                                            onclick = ev -> {
//                                                ev.stopPropagation();
//                                                if (JSExpression.of("window.confirm('Действительно удалить?')")) {
//                                                    conf.onRowDelete.handle(row.data);
//                                                    effect(rows = Array.filter(rows, r -> r != row));
//                                                }
//                                            };
//                                        }};
//                                    if (conf.expandedRow != null)
//                                        new div() {{
//                                            new div() {{ /* expand */
//                                                style.width = "20px";
//                                                style.height = "20px";
//                                                style.padding = "4px";
//
//                                                style.backgroundImage = row.expanded
//                                                    ? "url(\"data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyMCIgaGVpZ2h0PSIyMCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9ImN1cnJlbnRDb2xvciIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCIgY2xhc3M9ImZlYXRoZXIgZmVhdGhlci1jaGV2cm9uLXVwIj48cG9seWxpbmUgcG9pbnRzPSIxOCAxNSAxMiA5IDYgMTUiPjwvcG9seWxpbmU+PC9zdmc+\")"
//                                                    : "url(\"data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyMCIgaGVpZ2h0PSIyMCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9ImN1cnJlbnRDb2xvciIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCIgY2xhc3M9ImZlYXRoZXIgZmVhdGhlci1jaGV2cm9uLWRvd24iPjxwb2x5bGluZSBwb2ludHM9IjYgOSAxMiAxNSAxOCA5Ij48L3BvbHlsaW5lPjwvc3ZnPg==\")";
//
//                                                onclick = ev -> {
//                                                    ev.stopPropagation();
//                                                    row.expanded = !row.expanded;
//                                                    effect(row);
//                                                };
//                                            }};
//                                        }};
//                                }};
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
                                new div("grid-table-expansion-div") {{
                                    new slot<>(conf.expandedRow, row.data);
                                }};
                            }};
                        }};
                    }
                }
            }};
        }};
    }
}