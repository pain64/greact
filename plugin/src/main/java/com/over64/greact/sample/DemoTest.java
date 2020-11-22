package com.over64.greact.sample;

import com.over64.greact.dom.HtmlElement;
import com.over64.greact.model.components.Component;


public class DemoTest implements Component {
    @Override
    public void mount(HtmlElement dom) {
        {
            final com.over64.greact.dom.DocumentFragment $frag = com.over64.greact.dom.Globals.document.createDocumentFragment();
            final com.over64.greact.dom.HTMLNativeElements.div $el0 = com.over64.greact.dom.Globals.document.createElement("div");
            {
                $el0.className = "my-div";
                final com.over64.greact.dom.HTMLNativeElements.h1 $el1 = com.over64.greact.dom.Globals.document.createElement("h1");
                {
                    $el1.innerText = "hello, GReact";
                }
                $frag.appendChild($el1);
            }
            $frag.appendChild($el0);
            dom.appendChild($frag);
        }
    }
}
