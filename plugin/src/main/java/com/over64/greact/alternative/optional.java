package com.over64.greact.alternative;

import com.over64.greact.dom.HtmlElement;

import java.util.Optional;

public class optional<T extends HtmlElement, U> implements Component0<T> {
    final Component1<T, U> ifPresent;
    Component0<T> doElse = () -> null;

    final Optional<U> value;

    public optional(Optional<U> value, Component1<T, U> ifPresent) {
        this.value = value;
        this.ifPresent = ifPresent;
    }

    @Override
    public T mount() {
        return value.isPresent()
            ? ifPresent.mount(value.get())
            : doElse.mount();
    }
}
