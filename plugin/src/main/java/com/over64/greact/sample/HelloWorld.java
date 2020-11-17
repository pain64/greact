package com.over64.greact.sample;

import com.over64.greact.GReact;
import com.over64.greact.model.components.Component;
import com.over64.greact.model.components.HTMLNativeElements.h1;
import org.over64.jscripter.std.js.HTMLElement;

public class HelloWorld implements Component {
    @Override public void mount(HTMLElement dom) {
        GReact.mount(dom, new h1("Hello, world"));
    }
}
