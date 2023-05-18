package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class img extends HTMLElement implements HTMLElementAsComponent<img> {
    public String src;
    public String align;
}
