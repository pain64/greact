package com.over64.greact.alternative;

import com.over64.greact.dom.HTMLNativeElements.*;

public class HW implements Component2<div> {
    int nUsers = 1;

    @Override public void mount() {
        render(new div() {{
            new h1("number of GReact users: " + nUsers);
            new button("increment") {{
                onclick = () -> effect(nUsers + 1);
            }};
        }});
    }
}
