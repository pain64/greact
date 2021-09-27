package modifier.slot;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

import static util.CompileAssert.assertCompiledMany;

public class Slot1 {
    @Test void slot1() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.ListDecorator",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                    class ListDecorator implements Component0<div> {
                        final String[] list;
                        Component1<h1, String> forDecorate = s -> null;
                                   
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
                    }""",
                """
                    package js;
                                       
                    import org.over64.jscripter.StdTypeConversion;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                       
                    class ListDecorator implements Component0<div> {
                        final String[] list;
                        Component1<h1, String> forDecorate = (s)->null;
                       \s
                        ListDecorator(String[] list) {
                            super();
                            this.list = list;
                        }
                       \s
                        @Override
                        public div mount() {
                            let _root = GReact.element;
                            return GReact.entry(() => {
                                _root.style.border = "1px red solid";
                                for (let s in list) {
                                    GReact.make(_root, 'div', _el0 => {
                                        _el0.style.border = "1px green solid";
                                        GReact.mountSlot(_el0, 'h1', forDecorate, new Object[]{s});
                                    });
                                }
                            });
                        }
                    }"""),
            new CompileAssert.CompileCase("js.Demo",
                """
                    package js;
                                   
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                   
                    public class Demo implements Component0<div> {                                   
                        @Override
                        public div mount() {
                            var list = new String[]{"one", "two", "three"};
                            return new div() {{
                                new ListDecorator(list) {{
                                    forDecorate = s -> new h1("text:" + s);
                                }};
                            }};
                        }
                    }""",
                """
                    package js;

                    import org.over64.jscripter.StdTypeConversion;
                    import com.over64.greact.dom.HTMLNativeElements.*;

                    public class Demo implements Component0<div> {
                       \s
                        public Demo() {
                            super();
                        }
                       \s
                        @Override
                        public div mount() {
                            final com.over64.greact.dom.HTMLNativeElements.div $root = (com.over64.greact.dom.HTMLNativeElements.div)com.over64.greact.dom.Globals.gReactElement;
                            java.lang.String[] list = new String[]{"one", "two", "three"};
                            return com.over64.greact.dom.Globals.gReactReturn(()->{
                                final com.over64.greact.dom.HTMLNativeElements.div $el0 = com.over64.greact.dom.Globals.document.createElement("div");
                                final js.Demo$1$1 $comp0 = new ListDecorator(list){
                                   \s
                                    (java.lang.String[] list) {
                                        super(list);
                                    }
                                    {
                                        forDecorate = (s)->{
                                            final com.over64.greact.dom.HTMLNativeElements.h1 $root = (com.over64.greact.dom.HTMLNativeElements.h1)com.over64.greact.dom.Globals.gReactElement;
                                            return com.over64.greact.dom.Globals.gReactReturn(()->{
                                                $root.innerText = "text:" + s;
                                            });
                                        };
                                    }
                                };
                                com.over64.greact.dom.Globals.gReactMount($el0, $comp0, new Object[]{});
                                $root.appendChild($el0);
                            });
                        }
                    }"""));
    }
}
