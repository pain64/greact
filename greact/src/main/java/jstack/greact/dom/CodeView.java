package jstack.greact.dom;

import jstack.jscripter.transpiler.model.Async;
import jstack.greact.dom.HTMLNativeElements.Component0;
import jstack.greact.dom.HTMLNativeElements.Component1;
import jstack.greact.dom.HTMLNativeElements.div;


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

    public CodeView(Component0<T> view, Component1<div, CodeAndView<T>> renderer) {
        throw new RuntimeException("will be delegated to constructor#2 at compile time");
    }

    /* constructor#2 */
    public CodeView(Component0<T> view,
                    Component1<div, CodeAndView<T>> renderer, String code) {

        this.view = view;
        this.renderer = renderer;
        this.code = code;
    }
    @Override @Async public Component0<div> mount() {
        return GReact.mmountAwaitView(renderer, new CodeAndView<>(code, view));
    }
}
