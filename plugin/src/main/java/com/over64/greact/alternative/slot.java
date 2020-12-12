package com.over64.greact.alternative;

import com.over64.greact.dom.HtmlElement;

public class slot<T extends HtmlElement> implements Component0<T> {

    public slot(Component0<T> comp) {
    }

    public <U1> slot(Component1<T, U1> comp, U1 u1) {
    }

    @Override
    public T mount() { return null; } // fake, implement on compile time
}
