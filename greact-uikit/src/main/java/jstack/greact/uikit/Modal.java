package jstack.greact.uikit;

import jstack.greact.dom.HTMLNativeElements.Component0;
import jstack.greact.dom.HTMLNativeElements.div;
import jstack.greact.dom.HTMLNativeElements.h4;
import jstack.greact.dom.HTMLNativeElements.slot;
import jstack.jscripter.transpiler.model.Require;
import jstack.greact.dom.HTMLElement;

@Require.CSS("modal.css") public class Modal<A extends HTMLElement, B extends HTMLElement> implements Component0<div> {
    final Component0<A> opener;
    final Component0<B> content;
    final String title;
    final int widthPercent;
    final int heightPercent;

    public Modal(String title, int widthPercent, int heightPercent,
                 Component0<A> opener, Component0<B> content
    ) {
        this.opener = opener;
        this.content = content;
        this.title = title;
        this.widthPercent = widthPercent;
        this.heightPercent = heightPercent;
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
                            if(widthPercent > 0)
                                style.width = widthPercent + "%";
                            if(heightPercent > 0)
                                style.height = "calc(" + heightPercent + "% - 100px)";

                            new div() {{
                                className = "modal-header";
                                new div(); // fake - for alignment
                                new h4(self.title) {{
                                    className = "modal-title";
                                }};
                                new div() {{
                                    className = "modal-close";
                                    new div("modal-i-close");
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
