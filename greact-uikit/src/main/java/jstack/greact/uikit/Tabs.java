package jstack.greact.uikit;

import jstack.jscripter.transpiler.model.Require;
import jstack.greact.dom.HTMLNativeElements;

@Require.CSS("tabs.css")
public class Tabs implements HTMLNativeElements.Component0<HTMLNativeElements.div> {
    final Tab[] tabs;
    private Tab selected;

    public Tabs(Tab... tabs) {
        this.tabs = tabs;
        if (this.tabs.length != 0)
            this.selected = this.tabs[0];
    }

    @Override public HTMLNativeElements.div mount() {
        return new HTMLNativeElements.div() {{
            new HTMLNativeElements.div() {{
                className = "tabs";
                for (var tab : tabs)
                    new HTMLNativeElements.span(tab.caption) {{
                        className = "tabs-content";
                        onclick = ev -> effect(selected = tab);

                        if (selected == tab)
                            className += " tabs-content-selected";
                    }};
            }};
            new HTMLNativeElements.div() {{
                className = "tabs-body";
                new HTMLNativeElements.slot<>(selected.view);
            }};
        }};
    }
}