package jstack.greact.dom;

import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI public class Window {
    public native double setTimeout(Runnable fn, int delayMillis);
    public native void alert(String text);
    public native void open(String link, String type);
}
