package com.over64.greact.uikit;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.*;

public class Tabs implements Component0<div> {

    final Tab[] tabs;
    private Tab selected;
    private boolean rerenderAll;

    public Tabs(Tab... tabs) {
        this.tabs = JSExpression.of("Array.from(arguments)"); // FIXME: fix varargs in JScripter
        if (this.tabs.length != 0)
            this.selected = this.tabs[0];
    }

    @Override
    public div mount() {
        return new div() {{
            new div() {{
                dependsOn = rerenderAll;
                new div() {{
                    style.display = "flex";
                    style.borderBottom = "1px solid #eee";
                    for (var tab : tabs)
                        new span(tab.caption) {{
                            style.padding = "5px 15px";
                            style.cursor = "pointer";
                            onclick = ev -> {
                                selected = tab;
                                effect(rerenderAll);
                            };

                            if (selected == tab)
                                style.backgroundColor = "#eee";
                        }};
                }};
                new div() {{
                    style.padding = "15px";
                    new slot<>(selected.view);
                }};
            }};
        }};
    }
}
