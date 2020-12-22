package com.over64.greact.sample;

import com.over64.greact.dom.HTMLNativeElements;
import com.over64.greact.dom.HTMLNativeElements.h1;

class Child implements HTMLNativeElements.Component0<h1> {
    int answer;

    @Override public h1 mount() {
        return new h1("hello");
    }
}
