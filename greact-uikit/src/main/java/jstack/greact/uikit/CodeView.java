package jstack.greact.uikit;

import jstack.greact.dom.GReact;
import jstack.greact.dom.HTMLElement;
import jstack.greact.html.Component0;
import jstack.greact.html.Component1;
import jstack.greact.html.div;

public class CodeView<T extends HTMLElement> implements Component0<div> {
    public static class CodeAndView<T extends HTMLElement> {
        public final String code;
        public final Component0<T> view;

        public CodeAndView(String code, Component0<T> view) {
            this.code = code;
            this.view = view;
        }
    }

    Component0<T> view;
    String code;
    Component1<div, CodeAndView<T>> renderer;

    public CodeView(Component0<T> view, Component1<div, CodeView.CodeAndView<T>> renderer) {
        throw new RuntimeException("will be delegated to constructor#2 at compile time");
    }

    /* constructor#2 */
    public CodeView(Component0<T> view,
                    Component1<div, CodeView.CodeAndView<T>> renderer, String code) {

        this.view = view;
        this.renderer = renderer;
        this.code = code;
    }
    @Override public Component0<div> render() {
        return GReact.renderAwaitView(renderer, new CodeView.CodeAndView<>(code, view));
    }
}