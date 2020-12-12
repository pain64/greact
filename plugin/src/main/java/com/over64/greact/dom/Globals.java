package com.over64.greact.dom;

import com.greact.model.JSExpression;
import com.over64.greact.model.components.Component;

public class Globals {
    public static Window window = JSExpression.of("window");
    public static Document document = JSExpression.of("document");
    public static HtmlElement gReactElement;

    public static <T extends HtmlElement> void gReactMount(T dest, Component<T> element) {
        gReactElement = dest;
        element.mount();
    }
}
