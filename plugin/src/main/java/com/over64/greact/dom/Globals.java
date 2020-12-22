package com.over64.greact.dom;

import com.greact.model.JSExpression;

public class Globals {
    public static Window window = JSExpression.of("window");
    public static Document document = JSExpression.of("document");
    public static HtmlElement gReactElement;

    public static <T extends HtmlElement> void gReactMount(T dest, HTMLNativeElements.Component0<T> element) {
        gReactElement = dest;
        JSExpression.of("element instanceof Function ? element() : element.mount()");
    }

    public static <T extends HtmlElement> T gReactReturn(Fragment.Renderer renderer) {
        renderer.render();
        return null;
    }
}
