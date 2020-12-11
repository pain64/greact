package com.over64.greact.model.components;


import com.over64.greact.dom.HtmlElement;

public interface Component<T extends HtmlElement> extends Mountable {
    default void render(T newClass) {}
    default void effect(Object expression) {}
    void mount();
}
