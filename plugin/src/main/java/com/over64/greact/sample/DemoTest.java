package com.over64.greact.sample;

import com.over64.greact.dom.HTMLNativeElements;
import com.over64.greact.dom.HTMLNativeElements.div;
import com.over64.greact.dom.HtmlElement;
import com.over64.greact.model.components.Component;

import static com.over64.greact.dom.HTMLNativeElements.*;


public class DemoTest implements Component {
    @Override
    public void mount(HtmlElement dom) {
        {
            new div() {{
                fake.className = "123";
                new h1() {{
                    foobar = "123";
                }};
            }};

            final com.over64.greact.dom.DocumentFragment $frag = com.over64.greact.dom.Globals.document.createDocumentFragment();
            final div $el0 = com.over64.greact.dom.Globals.document.createElement("div");
            {
                $el0.className = "my-div";
                final h1 $el1 = com.over64.greact.dom.Globals.document.createElement("h1");
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
