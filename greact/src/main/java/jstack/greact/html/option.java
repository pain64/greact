package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class option extends HTMLElement implements HTMLElementAsComponent<option> {
    public boolean selected;
    public String value;
    public option(@DomProperty("innerText") String innerText) { }
}
