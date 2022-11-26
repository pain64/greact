package ui.analyze;

import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.async;
import jstack.greact.dom.*;
import jstack.greact.dom.HTMLNativeElements.*;

public class FindEffectCallsTest {
    /* annotate class or interface method */
    @async String fetchData() {
        return JSExpression.of("""
            await (
                await fetch('https://some.com')
            ).json()
            """);
    }
    /*
     * compiler force us to annotate because of call
     * async method
     */
    @async String concat() {
       return fetchData() + fetchData();
    }

    public static class MyPage implements HTMLNativeElements.Component0<HTMLNativeElements.div> {
        @Override public div mount() {
            return new div() {{
                new h1("hello, world");
            }};
        }
    }

    public static class ListDecorator implements Component0<div> {
        final String[] list;
        Component1<h1, String> forDecorate;

        ListDecorator(String[] list) {
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


    public static class Demo implements Component0<div> {
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

}
