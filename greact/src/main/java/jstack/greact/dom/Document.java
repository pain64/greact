package jstack.greact.dom;

import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI public class Document {
    public native HTMLElement getElementById(String id);
    public native <T extends HTMLElement> T createElement(String name);
    public native DocumentFragment createDocumentFragment();
}
