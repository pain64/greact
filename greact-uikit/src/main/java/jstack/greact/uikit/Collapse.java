package jstack.greact.uikit;

import jstack.greact.dom.HTMLElement;
import jstack.greact.dom.HTMLNativeElements.Component0;
import jstack.greact.dom.HTMLNativeElements.div;
import jstack.greact.dom.HTMLNativeElements.slot;
import jstack.jscripter.transpiler.model.Require;

@Require.CSS("navbar_and_dropdown.css")
public class Collapse implements Component0<div> {
    private final Component0<? extends HTMLElement>[] items;

    @SafeVarargs public Collapse(Component0<? extends HTMLElement>... args) {
        this.items = args;
    }

    @Override
    public Component0<div> mount() {
        return new div() {{
            className = "navbar-collapse";
            for (var item : items) new slot<>(item);
        }};
    }
}
