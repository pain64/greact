package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class tr extends HTMLElement implements HTMLElementAsComponent<tr> {
    public tr() { }
    public tr(HTMLElement child) { }
    public tr(td... childs) { }
}
