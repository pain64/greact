package com.over64.greact.uikit.samples.js;

import com.greact.model.CSS.Require;
import com.over64.greact.dom.HTMLNativeElements.Component0;
import com.over64.greact.dom.HTMLNativeElements.div;
import com.over64.greact.dom.HTMLNativeElements.h1;

@Require("main_page.css") public class MainPage implements Component0<div> {
    @Override public div mount() {
        return new div() {{
            new h1("hello, world") {{
                className = "my-h1";
            }};
        }};
    }
}
