package org.over64.jscripter.std.js;

import com.greact.model.JSNativeAPI;

@JSNativeAPI public class Document {
    public native HTMLElement getElementById(String id);
    public native HTMLElement createElement(String name);
}
