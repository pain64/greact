package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class canvas extends HTMLElement implements HTMLElementAsComponent<canvas> {
    public String width;
    public String height;
    public canvas() { }
    ;
}
