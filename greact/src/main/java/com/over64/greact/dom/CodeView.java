package com.over64.greact.dom;

import com.over64.greact.dom.HTMLNativeElements.Component0;
import com.over64.greact.dom.HTMLNativeElements.Component1;
import com.over64.greact.dom.HTMLNativeElements.div;


public class CodeView implements Component0<div> {
    public static class CodeAndView {
        public final String code;
        public final Component0<div> view;

        public CodeAndView(String code, Component0<div> view) {
            this.code = code;
            this.view = view;
        }
    }

    Component0<div> view;
    String code;
    Component1<div, CodeAndView> renderer;

    public CodeView(Component0<div> view, Component1<div, CodeAndView> renderer) {
        throw new RuntimeException("will be delegated to constructor#2 at compile time");
    }

    /* constructor#2 */
    public CodeView(Component0<div> view,
                    Component1<div, CodeAndView> renderer, String code) {

        this.view = view;
        this.renderer = renderer;
        this.code = code;
    }
    @Override public div mount() {
        var root = (div) GReact.element;
        GReact.mount(root, renderer, new Object[]{new CodeAndView(code, view)});
        return null;
    }
}
