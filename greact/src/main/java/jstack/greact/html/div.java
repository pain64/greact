package jstack.greact.html;

import jstack.greact.dom.HTMLElement;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI
public class div extends HTMLElement implements HTMLElementAsComponent<div> {
    public div() { }
    public div(@DomProperty("className") String className) { }
    public div(
        @DomProperty("className") String className,
        @DomProperty("title") String title
    ) { }
    public div(
        @DomProperty("className") String className,
        @DomProperty("title") String title,
        @DomProperty("onclick") MouseEventHandler onclick
    ) { }
}
