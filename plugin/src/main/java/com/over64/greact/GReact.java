package com.over64.greact;

import com.over64.greact.dom.HtmlElement;
import com.over64.greact.model.components.Mountable;

public class GReact {
    public static String classIf(boolean cond, String className) {
        return cond ? className : "";
    }
    public static void mount(HtmlElement dom, Mountable component) {

    }

    public static void effect(Object expr) {

    }
}
