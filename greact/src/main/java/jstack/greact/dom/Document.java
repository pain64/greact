package jstack.greact.dom;

import jstack.jscripter.transpiler.model.JSNativeAPI;

import java.util.function.Consumer;

@JSNativeAPI public class Document {
    public native HTMLElement getElementById(String id);
    public native <T extends HTMLElement> T createElement(String name);
    public native DocumentFragment createDocumentFragment();
    public native void addEventListener(String type, Consumer<HTMLElement.Event> runnable);
    public native void removeEventListener(String type, Consumer<HTMLElement.Event> runnable);
}
