package com.over64.greact.sample;

import com.over64.greact.GReact;
import com.over64.greact.dom.HTMLNativeElements.h1;
import com.over64.greact.dom.HtmlElement;
import com.over64.greact.model.components.Component;

public class HelloWorld implements Component {
    @Override public void mount(HtmlElement dom) {
        GReact.mount(dom, new h1("Hello, world"));
    }
}
