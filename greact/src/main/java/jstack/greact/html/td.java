package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class td extends HTMLElement implements HTMLElementAsComponent<td> {
    public int colSpan;
    public int rowSpan;
    public td() { }
    public td(@DomProperty("innerText") String innerText) { }
}
