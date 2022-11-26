package jstack.greact.uikit;

import jstack.jscripter.transpiler.model.Require;
import jstack.greact.dom.HTMLElement;
import jstack.greact.dom.HTMLNativeElements;

@Require.CSS("modal.css") public class Modal<A extends HTMLElement, B extends HTMLElement> implements HTMLNativeElements.Component0<HTMLNativeElements.div> {
    final HTMLNativeElements.Component0<A> opener;
    final HTMLNativeElements.Component0<B> content;
    public String title = "";
    public int widthPercent = -1;
    public int heightPercent = -1;

    public Modal(HTMLNativeElements.Component0<A> opener, HTMLNativeElements.Component0<B> content) {
        this.opener = opener;
        this.content = content;
    }

    boolean opened = false;

    @Override public HTMLNativeElements.div mount() {
        var self = this; //FIXME: HTMLElement has title attribute?
        return new HTMLNativeElements.div() {{
            if (opened)
                new HTMLNativeElements.div() {{
                    className = "modal";
                    new HTMLNativeElements.div() {{
                        className = "modal-body";
                        new HTMLNativeElements.div() {{
                            className = "modal-content";
                            if(widthPercent > 0) style.width = widthPercent + "%";
                            if(heightPercent > 0) style.minHeight = heightPercent + "%";
                            new HTMLNativeElements.div() {{
                                className = "modal-header";
                                new HTMLNativeElements.h3(self.title) {{
                                    className = "modal-title";
                                }};
                                new HTMLNativeElements.div() {{
                                    className = "modal-close";
                                    innerHTML = """
                                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-x"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                                        """;
                                    onclick = ev -> {
                                        ev.stopPropagation();
                                        effect(opened = false);
                                    };
                                }};
                            }};
                            new HTMLNativeElements.slot<>(content);
                        }};
                    }};
                    onclick = ev -> ev.stopPropagation();
                }};

            new HTMLNativeElements.div() {{
                onclick = ev -> {
                    ev.stopPropagation();
                    effect(opened = true);
                };
                new HTMLNativeElements.slot<>(opener);
            }};
        }};
    }
}
