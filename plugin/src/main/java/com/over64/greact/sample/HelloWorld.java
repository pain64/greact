package com.over64.greact.sample;

import com.over64.greact.dom.HTMLNativeElements.h1;
import com.over64.greact.model.components.Component;

public class HelloWorld implements Component<h1> {
    @Override public h1 mount() {
        return new h1("Hello, world");
    }
}
