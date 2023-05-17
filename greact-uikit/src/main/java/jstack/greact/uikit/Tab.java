package jstack.greact.uikit;

import jstack.greact.html.Component0;
import jstack.greact.html.div;
import jstack.greact.html.slot;

public class Tab { // FIXME: make inner class of Tabs?
    final String caption;
    final Component0<div> view;
    boolean selected = false;

    public Tab(String caption, Component0<div> view) {
        this.caption = caption;
        this.view = new div() {{ new slot<>(view); }};
    }
}
