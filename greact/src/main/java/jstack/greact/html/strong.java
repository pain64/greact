package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class strong extends HTMLElement implements HTMLElementAsComponent<strong> {
    public strong() { }
    public strong(@DomProperty("innerText") String innerText) { }
}
