package jstack.greact.dom;

import jstack.jscripter.transpiler.model.Async;
import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI public class Clipboard {
    @Async public native void writeText(String text);
}
