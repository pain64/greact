package jstack.greact.uikit;

import jstack.greact.dom.HTMLNativeElements.Component0;
import jstack.greact.dom.HTMLNativeElements.div;

public class Tab { // FIXME: make inner class of Tabs?
    final String caption;
    final Component0<div> view;
    boolean selected = false;

    public Tab(String caption, Component0<div> view) {
        this.caption = caption;
        this.view = view;
    }
}