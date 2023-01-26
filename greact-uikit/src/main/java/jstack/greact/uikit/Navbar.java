package jstack.greact.uikit;

import jstack.greact.dom.HTMLElement;
import jstack.greact.dom.HTMLNativeElements.*;
import jstack.jscripter.transpiler.model.Require;

@Require.CSS("navbar_and_dropdown.css")
public class Navbar implements Component0<nav> {
    public a brand;
    public Component0<? extends HTMLElement>[] content;

    @Override
    public Component0<nav> mount() {
        return new nav() {{
            className = "navbar navbar-expand-lg navbar-light";
            final div[] hideDiv = {new div()};

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
                    if (hideDiv[0].style.display.equals("none"))
                        hideDiv[0].style.display = "block";
                    else
                        hideDiv[0].style.display = "none";
                };
            }};

            for (var item : content) {
                if (item instanceof Collapse) {
                    hideDiv[0] = new div() {{
                        className = "navbar-collapse";
                        new slot<>(item);
                    }};
                    new slot<>(hideDiv[0]);
                } else {
                    new slot<>(item);
                }
            }
        }};
    }
}
