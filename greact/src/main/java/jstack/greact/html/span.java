package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class span extends HTMLElement implements HTMLElementAsComponent<span> {
    public span() { }
    public span(@DomProperty("innerText") String innerText) { }
}
