package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class h1 extends HTMLElement implements HTMLElementAsComponent<h1> {
    public h1() { }
    ;
    public h1(@DomProperty("innerText") String innerText) { }
}
