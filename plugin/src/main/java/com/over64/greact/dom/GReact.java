package com.over64.greact.dom;

import com.greact.model.JSExpression;

import java.util.function.Consumer;

import static com.over64.greact.dom.Globals.document;

public class GReact {
    public static HtmlElement element;
    public static <T extends HtmlElement> T entry(java.lang.Runnable maker) {
        maker.run();
        return null;
    }

    public static <T extends HtmlElement> T mount(T dest, HTMLNativeElements.Component<T> newEl, Object... args) {
        element = dest;
        JSExpression.of("newEl instanceof Function ? newEl(...args) : newEl.mount(...args)");
        return null;
    }

    public static <U extends HtmlElement> void make(HtmlElement parent, String name, Consumer<U> maker) {
        U el = document.createElement(name);
        maker.accept(el);
        parent.appendChild(el);
    }
}
