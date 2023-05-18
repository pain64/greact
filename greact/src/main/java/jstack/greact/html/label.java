package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class label extends HTMLElement implements HTMLElementAsComponent<label> {
    public label() { }
    public label(@DomProperty("innerText") String innerText) { }
    public String _for;
}
