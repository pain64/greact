package com.over64.greact.sample;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.model.components.Component;

public class DemoTest implements Component<div> {
    static class Decorator implements Component<div> {
        Component<h1> forDecorate = () -> null;

        @Override public div mount() {
            return new div() {{
                style.border = "1px red solid";
                new slot(forDecorate);
            }};
        }
    }

    @Override
    public div mount() {
        return new div() {{
            new Decorator() {{
                forDecorate = () -> new h1("decorated text!");
            }};
        }};
    }
}
