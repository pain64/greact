package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class li extends HTMLElement implements HTMLElementAsComponent<li> {
    public li() { }
    public li(@DomProperty("className") String className) { }
}
