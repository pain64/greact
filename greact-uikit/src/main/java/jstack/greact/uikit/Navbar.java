package jstack.greact.uikit;

import jstack.greact.dom.HTMLNativeElements.*;
import jstack.jscripter.transpiler.model.Require;

@Require.CSS("navbar.css")
public class Navbar implements Component0<nav> {
    private final Item[] items;
    private final a brand;

    public Navbar(a brand, Item... args) {
        this.items = args;
        this.brand = brand;
        this.brand.className = "navbar-brand";
    }
    @Override
    public Component0<nav> mount() {
        return new nav() {{
            className = "navbar navbar-expand-lg navbar-light";
            final div[] hideDiv = {new div()};

            new slot<>(brand);

            new button() {{
                className = "navbar-toggler";
                new span() {{
                    className = "navbar-toggler-icon";
                    onclick = (ev) -> {
                        if (hideDiv[0].style.display.equals("none"))
                            hideDiv[0].style.display = "block";
                        else
                            hideDiv[0].style.display = "none";
                    };
                }};
            }};

            new div() {{
                hideDiv[0] = this;
                className = "navbar-collapse";
                new ul() {{
                    className = "navbar-nav";
                    for (var item : items) {
                        new li() {{
                            className = "nav-item";
                            new slot<>(item.view);
                        }};
                    }
                }};
            }};
        }};
    }
}
