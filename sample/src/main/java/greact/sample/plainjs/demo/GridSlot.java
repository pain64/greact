package greact.sample.plainjs.demo;

import com.greact.model.JSExpression;
import com.greact.model.async;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.dom.HtmlElement;
import greact.sample.plainjs.demo.searchbox._01impl.Cascade;

import java.util.function.Consumer;

public class GridSlot<T> implements Component1<div, T[]> {
    Object dependsOn;
    Column<T>[] columns = (Column<T>[]) new Object[0];
    Component1<div, T> expandedRow;
    @FunctionalInterface public interface AsyncHandler<T> {
        @async void handle(T value);
    }
    AsyncHandler<T> onRowDelete;

//    public Grid(T[] data) {
//        this.list = arrayMap(data, v -> new _00Row<>(v));
//        // FIXME: нужно разобраться с VarArgs
//        //this.columns = JSExpression.of("[].slice.call(arguments, 1)");
//    }

    <A> boolean strictEqual(A lhs, A rhs) { return JSExpression.of("lhs === rhs"); }
    <A, B> B[] arrayMap(A[] from, Cascade.Func1<A, B> mapper) {
        return JSExpression.of("from.map(mapper)");
    }
    <A> A[] arrayFilter(A[] from, Cascade.Func1<A, Boolean> predicate) {
        return JSExpression.of("from.filter(predicate)");
    }

    private _00Row<T>[] list;
    private boolean rerenderAll;
    private HtmlElement theTable;


    @Override public div mount(T[] data) {
        this.list = arrayMap(data, v -> new _00Row<>(v));

        return new div() {{
            new div() {{
                dependsOn = rerenderAll;
                new style("""
                    .table {
                       border-collapse: collapse;
                       border-spacing: 0;
                       width: 100%;
                       cellspacing: 1px;
                    }
                    .table > tbody > tr {
                        line-height: 40px;
                    }
                    .table > thead {
                        border-bottom: 2px  solid black;
                        border-collapse: separate;
                    }
                                        
                    .table > thead > tr > td {
                        font-weight: 500;
                    }
                    .table-striped > tbody > tr:nth-child(even) {
                      background-color: #f2f2f2;
                    }
                    .table > tbody > tr:hover:not(.expansion-row) {
                        background-color: #ddf4d1;
                    }
                    .toolbox {
                      visibility: hidden;
                    }
                    tr:hover > td > .toolbox {
                      visibility: visible;
                    }
                    .expansion-row > td {
                      margin-bottom: 1px;
                    }
                    .toolbox > div {
                      margin: 0px 1px 0px 1px;
                    }
                    .toolbox > div:hover {
                      background-color: #ffbbc7;
                    }
                    """);
                new table() {{
                    theTable = this;
                    className = "table table-striped";
                    style.margin = "20px 0px 0px 0px";
                    new thead() {{
                        new tr() {{
                            for (var col : columns)
                                new td() {{
                                    new span(col.header);
                                }};
                            new td("") {{
                                style.width = "54px";
                            }};
                        }};
                    }};
                    new tbody() {{
                        for (var row : list) {
                            new tr() {{
                                style.cursor = "pointer";
                                onclick = ev -> {
                                    row.selected = !row.selected;
                                    effect(rerenderAll);
                                };

                                for (var col : columns)
                                    new td(col.rowData.fetch(row.data).toString());

                                new td() {{
                                    new div() {{
                                        style.display = "flex";
                                        className = "toolbox";
                                        new div() {{
                                            innerHTML = """
                                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-minus"><line x1="5" y1="12" x2="19" y2="12"/></svg>
                                                """;
                                            onclick = ev -> {
                                                ev.stopPropagation();
                                                list = arrayFilter(list, r -> !strictEqual(row, r));
                                                onRowDelete.handle(row.data);
                                                effect(rerenderAll);
                                            };
                                        }};
                                        new div() {{
                                            innerHTML = """
                                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-edit"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                                                """;
                                        }};
                                        new div() {{
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
                                                effect(rerenderAll);
                                            };
                                        }};
                                    }};
                                }};

                                if (row.selected)
                                    style.backgroundColor = "#93d7ff";
                            }};

                            if (row.expanded) {
                                new tr(); // fake for stripe color save
                                new tr() {{
                                    className = "expansion-row";
                                    if (row.selected) style.backgroundColor = "#93d7ff";
                                    new td() {{
                                        colSpan = columns.length + 1;
                                        style.padding = "5px";
                                        style.borderBottom = "1px solid white";
                                        new div() {{
                                            style.backgroundColor = "white";
                                            style.display = "flex";
                                            style.justifyContent = "center";
                                            new slot<>(expandedRow, row.data);
                                        }};
                                    }};
                                }};
                            }
                        }
                    }};
                }};
            }};
        }};
    }
}