package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class form extends HTMLElement implements HTMLElementAsComponent<form> {
    public String action;
    public String name;
    public String method;
    public String acceptCharset;
}
