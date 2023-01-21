package jstack.greact.uikit;

import jstack.greact.dom.HTMLNativeElements.*;
import jstack.jscripter.transpiler.model.Require;

@Require.CSS("dropdown.css")
public class Dropdown implements Component0<div> {
    private final String innerTextForDropdown;
    private final Item[] items;

    public Dropdown(String innerText, Item... args) {
        this.innerTextForDropdown = innerText;
        this.items = args;
    }

    @Override
    public Component0<div> mount() {
        return new div() {{
            className = "dropdown";
            final div[] hideDiv = {new div()};

            new button(innerTextForDropdown) {{
                className = "dropdown-toggle btn";
                onclick = (ev) -> {
                    if (hideDiv[0].style.display.equals("block"))
                        hideDiv[0].style.display = "none";
                    else
                        hideDiv[0].style.display = "block";
                };
            }};

            new div() {{
                hideDiv[0] = this;
                className = "dropdown-menu";

                for (var item : items) {
                    new a() {{
                        className = "dropdown-item";
                        style.cursor = "pointer";
                        style.textDecoration = "none";
                        new slot<>(item.view);
                    }};
                }
            }};
        }};
    }
}
