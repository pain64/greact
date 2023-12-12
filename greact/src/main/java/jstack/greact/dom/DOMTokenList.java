package jstack.greact.dom;

import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI public class DOMTokenList {
    public native void add(String token);
    public native String remove(String token);
    public native boolean toggle(String token);
    public native boolean contains(String token);
}
