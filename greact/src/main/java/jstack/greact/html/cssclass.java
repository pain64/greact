package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class cssclass extends HTMLElement {
    public cssclass(String name, String css) { }
    public static String localClass(String name) { return name; }
}
