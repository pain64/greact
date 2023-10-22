package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class ul extends HTMLElement implements HTMLElementAsComponent<ul> {
    public ul() { }
    public ul(@DomProperty("className") String className) { }
}
