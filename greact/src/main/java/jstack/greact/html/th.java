package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class th extends HTMLElement implements HTMLElementAsComponent<th> {
    public th() { }
    public th(@DomProperty("innerText") String innerText) { }
}
