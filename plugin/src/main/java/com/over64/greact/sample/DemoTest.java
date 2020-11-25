package com.over64.greact.sample;

import com.over64.greact.GReact;
import com.over64.greact.dom.HTMLNativeElements;
import com.over64.greact.dom.HTMLNativeElements.div;
import com.over64.greact.dom.HtmlElement;
import com.over64.greact.model.components.Component;

import static com.over64.greact.dom.HTMLNativeElements.*;


public class DemoTest implements Component {
    int nUsers = 1;

    @Override
    public void mount(HtmlElement dom) {
        GReact.mount(dom, new div() {{
            new h1() {{ innerText = "GReact users: " + nUsers; }};
            new button() {{
                innerText = "increment";
                onclick = () -> {
                    nUsers += 1;
                    GReact.effect(nUsers);
                };
            }};
        }});

        GReact.mount(dom, new div() {{
            var z = nUsers;
            new h1("GReact users: " + z);
            new button("increment") {{
                onclick = () -> GReact.effect(nUsers += 1);
            }};
        }});
    }
}
