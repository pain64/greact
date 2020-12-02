package com.over64.greact.sample;

import com.over64.greact.GReact;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.dom.HtmlElement;
import com.over64.greact.model.components.Component;


public class DemoTest implements Component {
    int nUsers = 1;
    int[] list = new int[]{1, 2, 3};


    @Override
    public void mount(HtmlElement dom) {


        new ul() {{
            dependsForRemount = list;
            for (var x : list)
                new li() {{ new a("text:" + x); }};
        }};

        GReact.mount(dom, new div() {{
            new h1("GReact users: " + nUsers);
            new button("increment") {{
                onclick = () -> GReact.effect(nUsers += 1);
            }};
        }});
    }
}
