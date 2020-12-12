package com.over64.greact.alternative;

import com.over64.greact.dom.HtmlElement;

import java.util.function.Supplier;

public interface Component1<T extends HtmlElement, U1> {
    default void effect(Object expression) {}
    default <U> U server(Supplier<U> onServer) { return null; }
    T mount(U1 u1);
}
