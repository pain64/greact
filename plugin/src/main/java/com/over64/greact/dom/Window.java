package com.over64.greact.dom;

import com.greact.model.JSNativeAPI;

@JSNativeAPI public class Window {
    public native double setTimeout(Runnable fn, int delayMillis);
    public native void alert(String text);
}
