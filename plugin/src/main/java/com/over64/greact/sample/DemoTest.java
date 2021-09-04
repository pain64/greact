package com.over64.greact.sample;

import com.greact.model.JSExpression;
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

        Runnable me;

        static Runnable $render(Runnable fragment) {
            fragment.run();
            return fragment;
        }

        int bar = 0;

        @Override public div mount() {
            return new div() {{
                style.border = "1px red solid";
                new div() {{
                    for (var s : list)
                        new div() {{
                            Runnable me = () -> {
                                var _me = JSExpression.<Runnable>of("me");
                                style.border = "1px green solid" + bar;
                                new slot<>(forDecorate, s);
                                _me.run();
                            };
                            me.run();
                        }};
                }};

                new div() {{ onclick = ev -> effect(bar = 42); }};
            }};
        }
    }

    @Override public div mount() {
        var list = new String[]{"one", "two", "three"};
        return new div() {{
            new ListDecorator(list) {{
                forDecorate = s -> new h1("text:" + s);
            }};
        }};
    }
}
