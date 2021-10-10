package com.over64.greact.sample;

import com.over64.greact.dom.HTMLNativeElements;
import com.over64.greact.dom.HTMLNativeElements.Component0;
import com.over64.greact.dom.HTMLNativeElements.h1;

public class HelloWorld implements Component0<h1> {
    @Override public h1 mount() {
        return new h1("Hello, world");
    }
}
