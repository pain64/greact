package jstack.greact.dom;

import jstack.jscripter.transpiler.model.Async;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI public class Document {
    @FunctionalInterface public interface EventListener {
        @Async void handle(HTMLElement.Event ev);
    }
    public native HTMLElement getElementById(String id);
    public native <T extends HTMLElement> T createElement(String name);
    public native DocumentFragment createDocumentFragment();
    public native void addEventListener(String type, EventListener handler);
    public native void removeEventListener(String type, EventListener handler);
    public HTMLElement activeElement;
}
