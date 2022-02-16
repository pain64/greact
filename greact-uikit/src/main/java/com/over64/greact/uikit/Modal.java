package com.over64.greact.uikit;

import com.greact.model.Require;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.dom.HTMLElement;

@Require.CSS("modal.css") public class Modal<A extends HTMLElement, B extends HTMLElement> implements Component0<div> {
    final Component0<A> opener;
    final Component0<B> content;
    public String title = "";
    public int widthPercent = -1;
    public int heightPercent = -1;

    public Modal(Component0<A> opener, Component0<B> content) {
        this.opener = opener;
        this.content = content;
    }

    boolean opened = false;

    @Override public div mount() {
        var self = this; //FIXME: HTMLElement has title attribute?
        return new div() {{
            if (opened)
                new div() {{
                    className = "modal";
                    new div() {{
                        className = "modal-body";
                        new div() {{
                            className = "modal-content";
                            if(widthPercent > 0) style.width = widthPercent + "%";
                            if(heightPercent > 0) style.minHeight = heightPercent + "%";
                            new div() {{
                                className = "modal-header";
                                new h3(self.title) {{
                                    className = "modal-title";
                                }};
                                new div() {{
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
                            new slot<>(content);
                        }};
                    }};
                    onclick = ev -> ev.stopPropagation();
                }};

            new div() {{
                onclick = ev -> {
                    ev.stopPropagation();
                    effect(opened = true);
                };
                new slot<>(opener);
            }};
        }};
    }
}
