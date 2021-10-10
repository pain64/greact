package com.over64.greact.uikit;

import com.over64.greact.dom.HTMLNativeElements.*;

public class Tab { // FIXME: make inner class of Tabs?
    final String caption;
    final Component0<div> view;
    boolean selected = false;

    public Tab(String caption, Component0<div> view) {
        this.caption = caption;
        this.view = view;
    }
}
