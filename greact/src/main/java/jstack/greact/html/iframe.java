package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class iframe extends HTMLElement implements HTMLElementAsComponent<iframe> {
    public String sandbox;
    public String src;
}
