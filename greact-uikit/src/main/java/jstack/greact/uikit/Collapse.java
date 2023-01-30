package jstack.greact.uikit;

import jstack.greact.dom.HTMLElement;
import jstack.greact.dom.HTMLNativeElements.Component0;
import jstack.greact.dom.HTMLNativeElements.div;
import jstack.greact.dom.HTMLNativeElements.slot;
import jstack.jscripter.transpiler.model.Require;

@Require.CSS("navbar.css")
public class Collapse implements Component0<div> {
    private final Component0<? extends HTMLElement>[] items;
    private div view;

    @SafeVarargs public Collapse(Component0<? extends HTMLElement>... args) {
        this.items = args;
    }

    public void changeVisible() {
        if (this.view.style.display.equals("none"))
            this.view.style.display = "block";
        else
            this.view.style.display = "none";
    }

    @Override
    public Component0<div> mount() {
        this.view = new div() {{
            className = "collapse navbar-collapse";
            for (var item : items) new slot<>(item);
        }};
        return view;
    }
}
