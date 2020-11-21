package com.over64.greact.sample;

import com.over64.greact.GReact;
import com.over64.greact.model.components.Component;
import com.over64.greact.model.components.HTMLNativeElements.div;
import org.over64.jscripter.std.js.HTMLElement;

public class DemoTest implements Component {
    @Override public void mount(HTMLElement dom) {
        final org.over64.jscripter.std.js.DocumentFragment $frag = org.over64.jscripter.std.js.Globals.document.createDocumentFragment();
        final com.over64.greact.model.components.HTMLNativeElements.div $el1 = new div();
        $frag.appendChild($el1);
        $frag.appendChild(dom);
        dom.appendChild($frag);
        //GReact.mount(dom, );
    }
}
