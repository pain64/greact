package com.over64.greact.alternative;

import com.over64.greact.dom.HtmlElement;

import java.util.function.Supplier;

public interface Component0<T extends HtmlElement> {
    default void effect(Object expression) {}
    default <U> U server(Supplier<U> onServer) { return null; }
    T mount();
}
