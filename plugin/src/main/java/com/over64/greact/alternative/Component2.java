package com.over64.greact.alternative;

import com.over64.greact.dom.HtmlElement;

public interface Component2<T extends HtmlElement> {
    default void render(T element) {}
    default void effect(Object expression) {}
    void mount();
}
