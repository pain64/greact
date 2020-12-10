package com.over64.greact.model.components;


import com.over64.greact.dom.HtmlElement;

public interface Component<T extends HtmlElement> extends Mountable {
    void mount(T dom);
}
