package jstack.greact.uikit;

import jstack.greact.html.Component0;
import jstack.greact.html.div;
import jstack.greact.html.slot;
import jstack.greact.html.span;
import jstack.jscripter.transpiler.model.Require;

@Require.CSS("tabs.css")
public class Tabs implements Component0<div> {
    final Tab[] tabs;
    private Tab selected;

    public Tabs(Tab... tabs) {
        this.tabs = tabs;
        if (this.tabs.length != 0)
            this.selected = this.tabs[0];
    }

    @Override public div render() {
        return new div() {{
            new div() {{
                className = "nav nav-tabs";
                for (var tab : tabs)
                    new span(tab.caption) {{
                        className = "nav-link";
                        onclick = ev -> effect(selected = tab);

                        if (selected == tab)
                            className += " active";
                    }};
            }};
            new div() {{
                className = "tabs-body";
                new slot<>(selected.getView());
            }};
        }};
    }
}