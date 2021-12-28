package com.over64.greact.uikit;

import com.greact.model.JSExpression;
import com.greact.model.async;
import com.over64.greact.dom.HTMLNativeElements.*;

import java.util.function.Consumer;

class GridFilter<T> implements Component0<div> {
    int[] pageSizes = new int[]{10, 20, 50, 100}; // FIXME: move to config ???
    int currentPage = 1;
    int currentSize = pageSizes[0];
    boolean filterEnabled = false;
    String filterValue = "";
    T[] pageData;

    final T[] data;
    final GridConfig2<T> conf;
    final Consumer<T> onRowSelect;

    GridFilter(T[] data, GridConfig2<T> conf, Consumer<T> onRowSelect) {
        this.data = data;
        this.conf = conf;
        this.onRowSelect = onRowSelect;
    }

    static <T> int calcNPages(T[] filtered, int currentSize) {
        var n = JSExpression.<Integer>of("Math.floor(filtered.length / currentSize)");
        if (filtered.length % currentSize != 0) return n + 1;
        else return n;
    }

    static int switchPage(int curr, int nPages, int diff) {
        var newCurrent = curr + diff;
        if (newCurrent < 1) return 1;
        if (newCurrent > nPages) return nPages;
        return newCurrent;
    }

    String[] stringSplit(String str, String delim) {
        return JSExpression.of("str.split(delim)");
    }
    int stringLength(String str) {
        return JSExpression.of("str.length");
    }

    void effectUnaffectedMe(Runnable ef) {
        ef.run();
    }

    @Override public div mount() {
        return new div() {{
            new div() {{
                var filterWords = Array.filter(
                        stringSplit(filterValue, " "),
                        s -> stringLength(s) != 0);

                T[] filtered = filterWords.length != 0 ?
                        Array.filter(data, v -> {
                            for (var col : conf.columns) {
                                var strVal = Grid.fetchValue(v, col.memberNames);
                                if (strVal == null) strVal = "";
                                strVal += ""; // FIXME: cast to string!!!
                                for (var fVal : filterWords)
                                    if (JSExpression.<Boolean>of("strVal.indexOf(fVal) != -1")) return true;
                            }
                            return false;
                        }) : data;

                var nPages = calcNPages(filtered, currentSize);
                var offset = (currentPage - 1) * currentSize;
                effectUnaffectedMe(() ->
                        effect(pageData = JSExpression.<T[]>of("filtered.slice(offset, offset + this.currentSize)")));

                if (filtered.length > pageSizes[0] || conf.title != null)
                    new div() {{
                        className = "grid-filter";

                        new div() {{
                            if (filtered.length > pageSizes[0]) {
                                new select() {{
                                    onchange = ev -> {
                                        // FIXME: move to one effect
                                        effect(currentSize = Integer.parseInt(ev.target.value));
                                        effect(currentPage = 1);
                                    };

                                    for (var size : pageSizes)
                                        new option("" + size) {{
                                            value = "" + size;
                                            selected = size == currentSize;
                                        }};
                                    className = "grid-filter-select";
                                }};
                                new span("записей на странице " + currentPage + " из " + nPages);
                            }
                        }};

                        new span(conf.title) {{
                            className = "grid-filter-span";
                        }};

                        new div() {{
                            if (filtered.length > pageSizes[0]) {
                                new div() {{
                                    innerHTML = """
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-chevron-left"><polyline points="15 18 9 12 15 6"/></svg>
                                        """;
                                    className = "page-turn";
                                    onclick = ev -> effect(currentPage = switchPage(currentPage, nPages, -1));
                                }};
                                new div() {{
                                    innerHTML = """
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-chevron-right"><polyline points="9 18 15 12 9 6"/></svg>
                                        """;
                                    className = "page-turn";
                                    onclick = ev -> effect(currentPage = switchPage(currentPage, nPages, 1));
                                }};
                            }
                        }};
                    }};
            }};
            if (filterEnabled)
                new div() {{
                    className = "grid-filter-enabled";
                    new input() {{
//                        value = filterValue; // one wat bindind
                        placeholder = "фильтр...";
                        className = "grid-filter-input";
                        onkeyup = ev -> effect(filterValue = ((input) ev.target).value);
                    }};
                }};
            new div() {{
                var hint = pageData;
                new slot<>(conf.pageView, new GridTable<>(pageData, conf, onRowSelect, () -> {
                    effect(filterEnabled = !filterEnabled);
                    effect(currentPage = 1);
                    effect(filterValue = "");
                }));
            }};
        }};
    }
}