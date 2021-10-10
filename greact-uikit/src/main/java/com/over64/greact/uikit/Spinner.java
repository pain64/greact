package com.over64.greact.uikit;

import com.greact.model.CSS;
import com.over64.greact.dom.HTMLNativeElements.*;

@CSS.Require("spinner.css")
public class Spinner implements Component0<div> {
    @Override public div mount() {
        return new div() {{
            className = "spinner";
            new span("working") {{ style.marginRight = "2px"; }};
            new div() {{ className = "bounce1"; }};
            new div() {{ className = "bounce2"; }};
            new div() {{ className = "bounce3"; }};
        }};
    }
}
