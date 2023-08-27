package jstack.greact.uikit;

import jstack.greact.dom.HTMLElement;
import jstack.greact.html.Component0;
import jstack.greact.html.li;
import jstack.greact.html.ul;
import jstack.greact.html.slot;
import jstack.greact.html.a;
import jstack.jscripter.transpiler.model.Require;

@Require.CSS("navbar.css")
public class NavbarNav implements Component0<ul> {
    private final Component0<? extends HTMLElement>[] items;

    @SafeVarargs public NavbarNav(Component0<? extends HTMLElement>... args) {
        this.items = args;
    }

    @Override
    public Component0<ul> render() {
        return new ul() {{
            className = "navbar-nav mr-auto";
            for (var item : items) {
                new li() {{
                    className = "nav-item";
                    new a() {{
                        className = "nav-link";
                        new slot<>(item);
                    }};
                }};
            }

        }};
    }
}
