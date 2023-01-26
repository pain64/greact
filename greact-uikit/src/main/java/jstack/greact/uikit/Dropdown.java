package jstack.greact.uikit;

import jstack.greact.dom.HTMLElement;
import jstack.greact.dom.HTMLNativeElements.*;
import jstack.jscripter.transpiler.model.Require;

@Require.CSS("navbar_and_dropdown.css")
public class Dropdown implements Component0<div> {
    public button dropdownButton;
    private final Component0<? extends HTMLElement>[] items;

    @SafeVarargs public Dropdown(String innerText, Component0<? extends HTMLElement>... args) {
        this.items = args;
        this.dropdownButton = new button(innerText);
    }

    @Override
    public Component0<div> mount() {
        return new div() {{
            className = "dropdown";
            final div[] hideDiv = {new div()};

            if (dropdownButton.className != null)
                dropdownButton.className += " dropdown-toggle";
            else dropdownButton.className = "dropdown-toggle";

            dropdownButton.onclick = (ev) -> {
                if (hideDiv[0].style.display.equals("block"))
                    hideDiv[0].style.display = "none";
                else
                    hideDiv[0].style.display = "block";
            };

            new slot<>(dropdownButton);

            new div() {{
                hideDiv[0] = this;
                className = "dropdown-menu";

                for (var item : items) {
                    new a() {{
                        className = "dropdown-item";
                        style.cursor = "pointer";
                        style.textDecoration = "none";
                        new slot<>(item);
                    }};
                }
            }};
        }};
    }
}
