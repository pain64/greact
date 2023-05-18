package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class select extends HTMLElement implements HTMLElementAsComponent<select> {
    public String name;
    public String value;
}
