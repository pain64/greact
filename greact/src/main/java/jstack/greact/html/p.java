package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class p extends HTMLElement implements HTMLElementAsComponent<p> {
    public p() { }
    public p(@DomProperty("innerText") String innerText) { }
}
