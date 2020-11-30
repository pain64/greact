package com.over64.greact.sample;

import com.over64.greact.GReact;
import com.over64.greact.dom.HTMLNativeElements.div;
import com.over64.greact.dom.HtmlElement;
import com.over64.greact.dom.ViewFragment;
import com.over64.greact.model.components.Component;

import static com.over64.greact.dom.HTMLNativeElements.button;
import static com.over64.greact.dom.HTMLNativeElements.h1;


public class DemoTest implements Component {
    int nUsers = 1;
    ViewFragment fragment1;

    void foo() {
        fragment1.apply();
    }

    @Override
    public void mount(HtmlElement dom) {
        (fragment1 = () -> {

        }).apply();

        GReact.mount(dom, new div() {{
            var z = nUsers;
            new h1("GReact users: " + z);
            new button("increment") {{
                onclick = () -> {
                    foo();
                    GReact.effect(nUsers += 1);
                };
            }};
        }});
    }
}
