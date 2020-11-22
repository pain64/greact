package com.over64.greact.model.components;


import com.over64.greact.dom.HtmlElement;

public interface Component extends Mountable {
    void mount(HtmlElement dom);
}
