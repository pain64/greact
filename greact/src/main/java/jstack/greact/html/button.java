package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class button extends HTMLElement implements HTMLElementAsComponent<button> {
    public String type;
    public button() { }
    public button(@DomProperty("innerText") String innerText) { }
}
