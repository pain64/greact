package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI public class a extends HTMLElement implements HTMLElementAsComponent<a> {
    public String href;
    public a() { }

    public a(@DomProperty("className") String className) { }

    public a(@DomProperty("className") String className,
             @DomProperty("innerText") String innerText) { }
}
