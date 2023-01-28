package jstack.greact.uikit;

import jstack.greact.dom.HTMLElement;
import jstack.greact.dom.HTMLNativeElements.*;
import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.Require;

@Require.CSS("navbar.css")
public class Navbar implements Component0<nav> {
    public a brand;
    public Component0<? extends HTMLElement>[] content;

    @Override
    public Component0<nav> mount() {
        return new nav() {{
            className = "navbar navbar-expand-lg bg-light";
            Collapse[] collapses = {};

            if (brand != null) {
                brand.className = "navbar-brand";
                new slot<>(brand);
            }

            new button() {{
                className = "navbar-toggler";
                new span() {{
                    className = "navbar-toggler-icon";
                }};
                onclick = (ev) -> {
                    for (var collapse : collapses)
                        collapse.changeVisible();
                };
            }};

            for (var item : content) {
                JSExpression.of("var c = null;"); // FIXME: instanceof
                if (item instanceof Collapse c) {
                    Array.push(collapses, c);
                    new slot<>(c);
                } else {
                    new slot<>(item);
                }
            }
        }};
    }
}
