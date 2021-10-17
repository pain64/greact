package com.over64.greact.uikit.samples.js;

import com.over64.greact.dom.CodeView;
import com.over64.greact.dom.CodeView.CodeAndView;
import com.over64.greact.dom.HTMLNativeElements.*;

public class CodeViewSample implements Component0<div> {
    Component1<div, CodeAndView> renderer = codeAndView ->
        new div() {{
            style.border = "1px red solid";
            new slot<>(codeAndView.view);
            new textarea() {{
                innerHTML = codeAndView.code.replaceAll("\n", "&#10").replaceAll(" ", "&nbsp");
                style.border = "1px green solid";
                style.height = "300px";
                style.width = "300px";
            }};
        }};

    @Override public div mount() {
        return new div() {{
            new h1("example1");
            new CodeView(() -> new div() {{
                new h1() {{
                    new h2("hello");
                    new div() {{
                        new h1("world");
                        var x = 1 + 1;
                    }};
                }};
            }}, renderer);

            new h1("example2");
            new CodeView(() -> new div() {{new h2("world");}}, renderer);
        }};
    }
}
