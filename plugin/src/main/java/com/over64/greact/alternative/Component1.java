package com.over64.greact.alternative;

import com.over64.greact.dom.HtmlElement;

import java.util.function.Supplier;

public interface Component1<T extends HtmlElement, U1> extends Component {
    T mount(U1 u1);
}
