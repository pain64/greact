package jstack.greact.uikit;

import jstack.greact.dom.Document;
import jstack.greact.dom.Globals;
import jstack.greact.dom.HTMLElement;
import jstack.greact.dom.HTMLNativeElements.Component0;
import jstack.greact.dom.HTMLNativeElements.a;
import jstack.greact.dom.HTMLNativeElements.div;
import jstack.greact.dom.HTMLNativeElements.slot;
import jstack.jscripter.transpiler.model.Require;

@Require.CSS("dropdown.css")
public class Dropdown implements Component0<div> {
    private Document.EventListener listener;
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

                listener = (event) -> {
                    if (!dropdown.contains(event.target) && !event.target.matches(".dropdown-menu"))
                        hideDiv[0].style.display = "none";
                    if (!hideDiv[0].isConnected)
                        Globals.document.removeEventListener("click", listener);
                };

                Globals.document.addEventListener("click", listener);

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
