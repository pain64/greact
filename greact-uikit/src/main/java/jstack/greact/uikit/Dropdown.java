package jstack.greact.uikit;

import jstack.greact.dom.HTMLElement;
import jstack.greact.dom.HTMLNativeElements.*;
import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.Require;

@Require.CSS("dropdown.css")
public class Dropdown implements Component0<div> {
    private final String innerTextForDropdown;
    private final Component0<? extends HTMLElement>[] items;

    @SafeVarargs public Dropdown(String innerText, Component0<? extends HTMLElement>... args) {
        this.items = args;
        this.innerTextForDropdown = innerText;
    }

    @Override
    public Component0<div> mount() {
        return new div() {{
            className = "dropdown";
            final div[] hideDiv = {new div()};
            var dropdown = this;

            new a(innerTextForDropdown) {{
                className = "dropdown-toggle";
                style.cursor = "pointer";
                this.onclick = (ev) -> {
                    if (hideDiv[0].style.display.equals("block"))
                        hideDiv[0].style.display = "none";
                    else
                        hideDiv[0].style.display = "block";
                };
            }};

            new div() {{
                hideDiv[0] = this;
                var hide = this;

                JSExpression.of("""
                 document.addEventListener('click', (event) => {
                   if (!(dropdown.contains(event.target)) && !event.target.matches('.dropdown-menu'))
                     hide.style.display = 'none';
                 });
                """);
                className = "dropdown-menu";

                for (var item : items) {
                    new div() {{
                        className = "dropdown-item";
                        new slot<>(item);
                    }};
                }
            }};
        }};
    }
}
