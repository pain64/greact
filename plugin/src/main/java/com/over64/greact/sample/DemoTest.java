package com.over64.greact.sample;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.dom.HtmlElement;
import com.over64.greact.model.components.Component;


public class DemoTest implements Component<div> {
    static class Conditional<T extends HtmlElement> implements Component<T> {
        boolean cond;
        Component<T> doThen = () -> null;
        Component<T> doElse = () -> null;

        T call(Component<T> comp) {
            return JSExpression.of("comp instanceof Function ? comp() : comp.mount()");
        }

        @Override
        public T mount() {
            return cond ? call(doThen) : call(doElse);
        }
    }

    boolean showHint = true;

    @Override
    public div mount() {
        return new div() {{
            new Conditional<h1>() {{
                cond = showHint;
                doThen = () -> new h1("This is the hint");
                doElse = () -> new h1("The hint is hidden");
            }};
            new button("show/hide") {{
                onclick = () -> effect(showHint = !showHint);
            }};
        }};
    }
}
