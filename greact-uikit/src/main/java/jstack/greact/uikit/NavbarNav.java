package jstack.greact.uikit;

import jstack.greact.dom.HTMLElement;
import jstack.greact.dom.HTMLNativeElements.*;
import jstack.greact.dom.HTMLNativeElements.Component0;
import jstack.greact.dom.HTMLNativeElements.ul;
import jstack.greact.dom.HTMLNativeElements.slot;
import jstack.jscripter.transpiler.model.Require;

@Require.CSS("navbar_and_dropdown.css")
public class NavbarNav implements Component0<ul> {
    private final Component0<? extends HTMLElement>[] items;

    @SafeVarargs public NavbarNav(Component0<? extends HTMLElement>... args) {
        this.items = args;
    }

    @Override
    public Component0<ul> mount() {
        return new ul() {{
            className = "navbar-nav";
            for (var item : items) {
                new li() {{
                    className = "nav-item";
                    new slot<>(item);
                }};
            }

        }};
    }
}
