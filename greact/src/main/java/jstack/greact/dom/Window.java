package jstack.greact.dom;

import jstack.jscripter.transpiler.model.Async;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI public class Window {
    public final Navigator navigator = new Navigator();

    public native double setTimeout(Runnable fn, int delayMillis);
    public native void alert(String text);
    public native void open(String link, String openType);

    public static class ResizeEvent extends HTMLElement.Event {}

    @FunctionalInterface public interface ResizeEventHandler {
        @Async void handle(ResizeEvent ev);
    }
    public ResizeEventHandler onresize;
}
