package greact.sample.plainjs.demo;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.*;

public class PaginationSlot<T> implements Component1<div, T[]> {

    // [50] записей на странице 1 из 3    < >
    // if n < stages.min => don't show pagination controls
    Component1<div, T[]> page;
    int[] pageSizes = new int[]{10, 20, 50, 100};
    int currentPage = 1;
    int currentSize = pageSizes[0];
    int nPages;
    T[] data;
    T[] pageData;
    boolean rerenderAll = true;

    void calcPage() {
        nPages = JSExpression.of("Math.floor(this.data.length / this.currentSize)");
        if (data.length % currentSize != 0) nPages++;
        var offset = (currentPage - 1) * currentSize;
        pageData = JSExpression.of("this.data.slice(offset, offset + this.currentSize)");
        // effect(rerenderAll);
    }

    void switchPage(int diff) {
        currentPage += diff;
        if(currentPage < 1) currentPage = 1;
        if(currentPage > nPages) currentPage = nPages;
        calcPage();
        effect(rerenderAll); // FIXME: move rerenderAll to calcPage
    }

    public void dropRowFor(T element) {
        data = JSExpression.of("this.data.filter(el => el !== element)");
        calcPage();
        effect(rerenderAll); // FIXME: move rerenderAll to calcPage
    }

    @Override
    public div mount(T[] data) {
        this.data = data;
        calcPage();

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

                new div() {{
                    style.display = "flex";
                    style.alignItems = "center";
                    new select() {{
                        onchange = ev -> {
                            currentSize = Integer.parseInt(ev.target.value);
                            currentPage = 1;
                            calcPage();
                            effect(rerenderAll);
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
                new slot<>(page, pageData);
            }};
        }};
    }
}
