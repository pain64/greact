package com.over64.greact.sample;

import com.over64.greact.dom.HTMLNativeElements.h1;
import com.over64.greact.model.components.Component;

class Child implements Component<h1> {
    int answer;

    @Override public h1 mount() {
        return new h1("hello");
    }
}