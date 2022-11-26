package jstack.greact.uikit;

import jstack.greact.dom.HTMLNativeElements;

public class Tab { // FIXME: make inner class of Tabs?
    final String caption;
    final HTMLNativeElements.Component0<HTMLNativeElements.div> view;
    boolean selected = false;

    public Tab(String caption, HTMLNativeElements.Component0<HTMLNativeElements.div> view) {
        this.caption = caption;
        this.view = view;
    }
}
