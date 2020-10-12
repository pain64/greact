package com.over64.greact.sample;

import org.over64.jscripter.std.js.DocumentFragment;

import static org.over64.jscripter.std.js.Globals.document;
import static org.over64.jscripter.std.js.Globals.window;

public class H1 {
    public H1(DocumentFragment dom, String text) {
        var me = document.createElement("h1");
        me.innerText = text;
        dom.appendChild(me);

        window.setTimeout(() -> me.innerText = "oops", 3000);
    }
}
