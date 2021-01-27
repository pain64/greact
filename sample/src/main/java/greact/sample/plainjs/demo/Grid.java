package greact.sample.plainjs.demo;

import com.greact.model.JSExpression;
import com.greact.model.async;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.dom.HtmlElement;
import greact.sample.plainjs.demo.searchbox.SearchBox;
import greact.sample.plainjs.demo.searchbox._00base.Indexed;
import greact.sample.plainjs.demo.searchbox._01impl.Cascade;

import java.util.function.Consumer;

public class Grid<T> implements Component0<div> {
    @FunctionalInterface
    public interface DataProvider<T> {
        void onData(T[] data);
    }

    public interface Searcher<T> extends Component1<div, DataProvider<T>> {
    }

    Searcher<T> data;
    Column<T>[] columns = (Column<T>[]) new Object[0];
    Component1<div, T> selectedRow;

    //    public Grid(T[] list) {
//        this.list = list;
//        // FIXME: нужно разобраться с VarArgs
//        //this.columns = JSExpression.of("[].slice.call(arguments, 1)");
//    }
    void log(Object obj) {
        JSExpression.of("console.log(obj)");
    }

    <A> boolean strictEqual(A lhs, A rhs) { return JSExpression.of("lhs === rhs"); }

    <A, B> B[] arrayMap(A[] from, Cascade.Func1<A, B> mapper) {
        return JSExpression.of("from.map(mapper)");
    }

    private _00Row<T>[] list = JSExpression.of("[]");
    private boolean rerenderAll;
    private HtmlElement theTable;


    @Override
    public div mount() {
        return new div() {{
            new div() {{
                dependsOn = rerenderAll;
                new slot<>(data, fetched -> effect(list = arrayMap(fetched, v -> new _00Row<>(v))));
                new style("""
                    .table {
                       border-collapse: collapse;
                       border-spacing: 0;
                       width: 100%;
                    }
                    .table > tbody > tr {
                        line-height: 40px; 
                    }
                    .table > thead {
                        border-bottom: 2px  solid black;
                        border-collapse: separate;
                    }
                                        
                    .table > thead > td {
                        font-weight: 500;
                    }
                    .table-striped > tbody > tr:nth-child(even) {
                      background-color: #f2f2f2;
                    }
                    .table > tbody > tr:hover:not(.expansion-row) {
                        background-color: #ddf4d1;
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
                                style.width = "16px";
                            }};
                        }};
                    }};
                    new tbody() {{
                        for (var row : list) {
                            new tr() {{
                                style.cursor = "pointer";
                                //className = row == current ? "table-active" : "";
                                onclick = ev -> {
                                    row.selected = !row.selected;
                                    effect(rerenderAll);
                                    //effect(list);
                                };
                                if (row.expanded) {
                                    new td() {{
                                        colSpan = columns.length + 1;
                                        new table() {{
                                            className = "table";
                                            new tbody() {{
                                                new tr() {{
                                                    for (var col : columns)
                                                        new td(col.rowData.fetch(row.data).toString());
                                                    new td() {{
                                                        style.width = "16px";
                                                        if (row.selected && !row.expanded) {
                                                            innerHTML = """
                                                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-corner-right-down"><polyline points="10 15 15 20 20 15"></polyline><path d="M4 4h7a4 4 0 0 1 4 4v12"></path></svg>
                                                                """;
                                                            onclick = ev -> {
                                                                ev.stopPropagation();
                                                                row.expanded = !row.expanded;
                                                                effect(rerenderAll);
                                                            };
                                                        }
                                                    }};
                                                }};
                                                new tr() {{
                                                    new td() {{
                                                        colSpan = columns.length + 1;
                                                        new div() {{
                                                            style.display = "flex";
                                                            style.padding = "5px";
                                                            style.justifyContent = "center";
                                                            new slot<>(selectedRow, row.data);
                                                        }};
                                                    }};
                                                }};
                                            }};
                                        }};
                                    }};
                                } else {
                                    for (var col : columns)
                                        new td(col.rowData.fetch(row.data).toString());
                                    new td() {{
                                        style.width = "16px";
                                        if (row.selected && !row.expanded) {
                                            innerHTML = """
                                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-corner-right-down"><polyline points="10 15 15 20 20 15"></polyline><path d="M4 4h7a4 4 0 0 1 4 4v12"></path></svg>
                                                """;
                                            onclick = ev -> {
                                                ev.stopPropagation();
                                                row.expanded = !row.expanded;
                                                effect(rerenderAll);
                                            };
                                        }
                                    }};
                                }

                                if (row.selected)
                                    style.backgroundColor = "#ddf4d1";

                            }};

//                            if (row.expanded) {
//                                new tr(); //fake row for same color
//                                new tr() {{
//                                    className = "expansion-row";
//                                    if(row.selected) style.backgroundColor = "#ddf4d1";
//                                    new td() {{
//                                        colSpan = columns.length;
//                                        //style.backgroundColor = "#fff9ad82";
//                                        new div() {{
//                                            style.display = "flex";
//                                            style.justifyContent = "center";
//                                            new slot<>(selectedRow, row.data);
//                                        }};
//                                    }};
//                                    new td() {{
//                                        style.width = "16px";
//                                        style.cursor = "pointer";
//                                        innerHTML = """
//                                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16"
//                                            viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
//                                            stroke-linecap="round" stroke-linejoin="round" class="feather
//                                            feather-corner-right-up"><polyline points="10 9 15 4 20 9"/><path d="M4
//                                            20h7a4 4 0 0 0 4-4V4"/></svg>
//                                            """;
//                                        onclick = ev -> {
//                                            ev.stopPropagation();
//                                            row.expanded = !row.expanded;
//                                            effect(rerenderAll);
//                                        };
//                                    }};
//                                }};
//                            }
                        }
                    }};
                }};
            }};
        }};
    }
}