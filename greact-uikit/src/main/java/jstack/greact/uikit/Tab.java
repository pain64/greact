package jstack.greact.uikit;

import jstack.greact.html.Component0;
import jstack.greact.html.div;
import jstack.greact.html.slot;

public class Tab {
    final String caption;
    private final Component0<div> view;
    private Component0<div> renderedView;

    public Tab(String caption, Component0<div> view) {
        this.caption = caption;
        this.view = view;
    }

    public Component0<div> getView() {
        if (renderedView == null)
            renderedView = new div() {{
                new slot<>(view);
            }};

        return renderedView;
    }
}
