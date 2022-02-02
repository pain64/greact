package com.over64.greact.uikit;

import com.greact.model.Require;
import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.*;

@Require.CSS("tabs.css")
public class Tabs implements Component0<div> {

    final Tab[] tabs;
    private Tab selected;

    public Tabs(Tab... tabs) {
        this.tabs = JSExpression.of("Array.from(arguments)"); // FIXME: fix varargs in JScripter
        if (this.tabs.length != 0)
            this.selected = this.tabs[0];
    }


    @Override public div mount() {
        return new div() {{
            new div() {{
                new div() {{
                    className = "tabs";
                    for (var tab : tabs)
                        new span(tab.caption) {{
                            className = "tabs-content";
                            onclick = ev -> effect(selected = tab);

                            if (selected == tab)
                                className += " tabs-content-selected";
                        }};
                }};
                new div() {{
                    className = "tabs-body";
                    new slot<>(selected.view);
                }};
            }};
        }};
    }
}