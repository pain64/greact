package com.over64.greact.sample;

import com.over64.greact.dom.Globals;
import com.over64.greact.dom.HTMLNativeElements.*;

public class DemoTest implements Component0<div> {
    static class ListDecorator implements Component0<div> {
        final String[] list;
        Component1<h1, String> forDecorate = s -> null;

        ListDecorator(String[] list) {
            Globals.gReactMount(null, null);
            this.list = list;
        }

        @Override public div mount() {
            return new div() {{
                style.border = "1px red solid";
                for (var s : list)
                    new div() {{
                        style.border = "1px green solid";
                        new slot<>(forDecorate, s);
                    }};
            }};
        }
    }

    @Override
    public div mount() {
        var list = new String[]{"one", "two", "three"};
        return new div() {{
            new ListDecorator(list) {{
                forDecorate = s -> new h1("text:" + s);
            }};
        }};
    }
}
