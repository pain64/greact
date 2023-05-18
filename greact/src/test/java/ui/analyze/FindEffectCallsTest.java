package ui.analyze;

import jstack.greact.html.*;
import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.Async;

public class FindEffectCallsTest {
    /* annotate class or interface method */
    @Async
    String fetchData() {
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
    @Async
    String concat() {
       return fetchData() + fetchData();
    }

    public static class MyPage implements Component0<div> {
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
