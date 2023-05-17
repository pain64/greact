package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class style extends HTMLElement implements HTMLElementAsComponent<jstack.greact.html.style> {
    public static String id(String prefix) { return prefix; }
    public style(@DomProperty("innerText") String innerText) { }
}
