package greact.sample.plainjs.demo;

import com.greact.model.JSExpression;
import com.greact.model.async;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.dom.HtmlElement;
import greact.sample.plainjs.demo.searchbox._00base.Pair;
import greact.sample.plainjs.demo.searchbox._01impl.Cascade;

import java.util.function.Function;

public class GridSlot<T> implements Component1<div, T[]> {
    Object dependsOn;
    Column<T, Object>[] columns = (Column<T, Object>[]) new Object[0];
    Component1<div, T> expandedRow;

    @FunctionalInterface
    public interface AsyncHandler<T> {
        @async
        void handle(T value);
    }

    AsyncHandler<T> onRowAdd;
    AsyncHandler<T> onRowChange;
    AsyncHandler<T> onRowDelete;

//    public Grid(T[] data) {
//        this.list = arrayMap(data, v -> new _00Row<>(v));
//        // FIXME: нужно разобраться с VarArgs
//        //this.columns = JSExpression.of("[].slice.call(arguments, 1)");
//    }

    <A> boolean strictEqual(A lhs, A rhs) { return JSExpression.of("lhs === rhs"); }

    <A, B> B[] arrayMap(A[] from, Function<A, B> mapper) {
        return JSExpression.of("from.map(mapper)");
    }

    <A> A[] arrayFilter(A[] from, Cascade.Func1<A, Boolean> predicate) {
        return JSExpression.of("from.filter(predicate)");
    }

    <A> void arrayPush(A[] array, A value) {
        JSExpression.of("array.push(value)");
    }

    <A, B> Pair<A, B>[] arrayZip(A[] left, B[] right) {
        Pair<A, B>[] result = (Pair<A, B>[]) new Object[0];
        for (var i = 0; i < left.length; i++)
            arrayPush(result, new Pair<>(left[i], right[i]));
        return result;
    }

    Object fetchValue(T rowData, String memberName) {
        return JSExpression.of("rowData[memberName]");
    }

    String toStrIfNotNullElseEmpty(Object rowData) {
        return rowData != null ? rowData.toString() : "";
    }

    void setValue(T rowData, String memberName, Object value) {
        JSExpression.of("rowData[memberName] = value");
    }

    @async void persistNewRow(T rowData) {
        onRowAdd.handle(rowData);
        var offset = (currentPage - 1) * currentSize;
        JSExpression.of("this.data.splice(offset, 0, rowData)");
    }

    private T[] data;
    private _00Row<T>[] list;
    private boolean rerenderAll;
    private boolean filterEnabled = false;
    private String filterValue = "";

    int[] pageSizes = new int[]{10, 20, 50, 100};
    int currentPage = 1;
    int currentSize = pageSizes[0];
    int nPages;
    T[] filtered;
    HtmlElement theTable;
    Integer[] columnSizes;

    void calcList() {
        var filterWords = arrayFilter(
            JSExpression.<String[]>of("this.filterValue.split(' ')"),
            s -> JSExpression.<Boolean>of("s.length != 0"));

        filtered = filterWords.length != 0 ?
            arrayFilter(data, v -> {
                for (var col : columns) {
                    var strVal = fetchValue(v, col.memberName).toString();
                    for (var fVal : filterWords)
                        if (JSExpression.<Boolean>of("strVal.indexOf(fVal) != -1")) return true;
                }
                return false;
            }) : data;

        nPages = JSExpression.of("Math.floor(this.filtered.length / this.currentSize)");
        if (filtered.length % currentSize != 0) nPages++;
        var offset = (currentPage - 1) * currentSize;
        var pageData = JSExpression.<T[]>of("this.filtered.slice(offset, offset + this.currentSize)");

        this.list = arrayMap(pageData, v -> new _00Row<>(v));
    }

    void disableFilterAndRerender() {
        this.filterValue = "";
        calcList();
        doRerender();
    }

    void switchPage(int diff) {
        currentPage += diff;
        if (currentPage < 1) currentPage = 1;
        if (currentPage > nPages) currentPage = nPages;
        calcList();
        doRerender(); // FIXME: move rerenderAll to calcPage
    }

    void doRerender() {
        this.columnSizes = arrayMap(columns, v -> 0);
        effect(rerenderAll);
    }

    void doRerenderAndKeepSize() {
        this.columnSizes = JSExpression.of("[].slice.call(this.theTable.tHead.rows[0].cells).map(e => e" +
            ".getBoundingClientRect().width)");
        effect(rerenderAll);
    }


    @Override
    public div mount(T[] data) {
        this.data = data;
        this.columnSizes = arrayMap(columns, v -> 0);
        calcList();

        return new div() {{
            new style("""
                .page-turn {
                  display: inline;
                  cursor: pointer;
                }
                .page-turn:hover {
                  background-color: #ffbbc7;
                  
                }
                """);
            new div() {{
                dependsOn = rerenderAll;

                if (filtered.length > pageSizes[0])
                    new div() {{
                        style.display = "flex";
                        style.alignItems = "center";
                        style.backgroundColor = "#eee";
                        style.padding = "5px";
                        new select() {{
                            onchange = ev -> {
                                currentSize = Integer.parseInt(ev.target.value);
                                currentPage = 1;
                                calcList();
                                doRerender();
                            };

                            for (var size : pageSizes)
                                new option("" + size) {{
                                    value = "" + size;
                                    selected = size == currentSize;
                                }};
                            style.marginRight = "10px";
                        }};
                        new span("записей на странице " + currentPage + " из " + nPages);
                        new div() {{
                            style.marginLeft = "auto";
                            new div() {{
                                innerHTML = """
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-chevron-left"><polyline points="15 18 9 12 15 6"/></svg>
                                    """;
                                className = "page-turn";
                                onclick = ev -> switchPage(-1);
                            }};
                            new div() {{
                                innerHTML = """
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-chevron-right"><polyline points="9 18 15 12 9 6"/></svg>
                                    """;
                                className = "page-turn";
                                onclick = ev -> switchPage(1);
                            }};
                        }};
                    }};
            }};

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
                    border-bottom: 1px  solid black;
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
                .toolbox-header > div:hover {
                  background-color: #ffbbc7;
                }
                """);

            if (filterEnabled)
                new div() {{
                    style.padding = "5px";
                    style.backgroundColor = "#eee";
                    new input() {{
                        value = filterValue;
                        placeholder = "фильтр...";
                        style.width = "100%";
                        onkeyup = ev -> {
                            filterValue = ((input) ev.target).value;
                            calcList();
                            doRerender();
                        };
                    }};
                }};
            else new div(); // FIXME: rerender bug!!!;

            new table() {{
                theTable = this;
                dependsOn = rerenderAll;
                className = "table table-striped";
                style.margin = "10px 0px 0px 0px";
                new thead() {{
                    new tr() {{
                        for (var colWithSize : arrayZip(columns, columnSizes))
                            new td() {{
                                style.width = colWithSize.b + "px";
                                new span(colWithSize.a.header);
                            }};
                        new td() {{
                            style.width = "54px";
                            new div() {{
                                style.display = "flex";
                                style.justifyContent = "flex-end";
                                className = "toolbox-header";
                                new div() {{
                                    style.width = "16px";
                                    innerHTML = """                           
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-filter"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon></svg>
                                        """;
                                    onclick = ev -> {
                                        if (filterEnabled) disableFilterAndRerender();
                                        effect(filterEnabled = !filterEnabled);
                                    };
                                }};
                                new div() {{
                                    style.width = "16px";
                                    innerHTML = """
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-plus"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                                        """;
                                    onclick = ev -> {
                                        effect(filterEnabled = false);
                                        disableFilterAndRerender();
                                        var rowData = JSExpression.<T>of("{}"); // FIXME: use prototype.new
                                        for (var col : columns)
                                            setValue(rowData, col.memberName, null);

                                        var newRow = new _00Row<>(rowData);
                                        newRow.editing = true;
                                        newRow.freshRow = true;
                                        JSExpression.of("this.list.splice(0, 0, newRow)");
                                        doRerenderAndKeepSize();
                                    };
                                }};
                            }};
                        }};
                    }};
                }};
                new tbody() {{
                    for (var row : list) {
                        new tr() {{
                            style.cursor = "pointer";
                            onclick = ev -> {
                                if (!row.editing) {
                                    row.selected = !row.selected;
                                    doRerender();
                                }
                            };

                            for (var col : columns)
                                new td() {{
                                    if (row.editing && col.editor != null) {
                                        col.editor.value = fetchValue(row.data, col.memberName);
                                        new slot<>(col.editor);
                                    } else innerText = toStrIfNotNullElseEmpty(fetchValue(row.data, col.memberName));
                                }};

                            new td() {{
                                new div() {{
                                    style.display = "flex";
                                    className = "toolbox";
                                    if (!row.editing) {
                                        new div() {{
                                            innerHTML = """
                                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-minus"><line x1="5" y1="12" x2="19" y2="12"/></svg>
                                                """;
                                            onclick = ev -> {
                                                ev.stopPropagation();
                                                list = arrayFilter(list, r -> !strictEqual(row, r));
                                                onRowDelete.handle(row.data);
                                                doRerender();
                                            };
                                        }};
                                        new div() {{
                                            innerHTML = """
                                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-edit"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                                                """;
                                            onclick = ev -> {
                                                ev.stopPropagation();
                                                row.editing = !row.editing;
                                                doRerenderAndKeepSize();
                                            };
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
                                                doRerender();
                                            };
                                        }};
                                    } else {
                                        new div() {{
                                            innerHTML = """
                                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-save"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/></svg>
                                                """;
                                            onclick = ev -> {
                                                ev.stopPropagation();
                                                // save changes
                                                for (var col : columns)
                                                    if (col.editor != null)
                                                        setValue(row.data, col.memberName, col.editor.value);

                                                if (row.freshRow) persistNewRow(row.data);
                                                else onRowChange.handle(row.data);
                                                row.editing = !row.editing;
                                                doRerender();
                                            };
                                        }};
                                        new div() {{
                                            innerHTML = """
                                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-x"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                                                """;
                                            onclick = ev -> {
                                                ev.stopPropagation();
                                                row.editing = !row.editing;
                                                doRerender();
                                            };
                                        }};
                                    }
                                }};
                            }};

                            if (row.editing)
                                style.backgroundColor = "#ffacac";
                            else if (row.selected)
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
    }
}