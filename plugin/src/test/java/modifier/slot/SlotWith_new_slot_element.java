package modifier.slot;

import org.junit.jupiter.api.Test;
import util.CompileAssert;
import util.CompileAssert.CompileCase;

import java.io.IOException;

import static util.CompileAssert.*;

public class SlotWith_new_slot_element {
    @Test void testSlotWith_new_slot_element() throws IOException {
        assertCompiledMany(
            new CompileCase("js.Decorator",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    class Decorator implements Component0<div> {
                        Component0<h1> forDecorate = () -> null;
                                    
                        @Override public div mount() {
                            return new div() {{
                                style.border = "1px red solid";
                                new slot<>(forDecorate);
                            }};
                        }
                    }
                    """,
                """
                    package js;
                                        
                    import org.over64.jscripter.StdTypeConversion;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    class Decorator implements Component0<div> {
                       \s
                        Decorator() {
                            super();
                        }
                        Component0<h1> forDecorate = ()->null;
                       \s
                        @Override
                        public div mount() {
                            final com.over64.greact.dom.HTMLNativeElements.div $root = (com.over64.greact.dom.HTMLNativeElements.div)com.over64.greact.dom.Globals.gReactElement;
                            return com.over64.greact.dom.Globals.gReactReturn(()->{
                                $root.style.border = "1px red solid";
                                final com.over64.greact.dom.HTMLNativeElements.h1 $el0 = com.over64.greact.dom.Globals.document.createElement("h1");
                                com.over64.greact.dom.Globals.gReactMount($el0, forDecorate);
                                $root.appendChild($el0);
                            });
                        }
                    }"""),
            new CompileCase("js.Demo",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                     
                    class Demo implements Component0<div> {                     
                        @Override public div mount() {
                            return new div() {{
                                new Decorator() {{
                                    forDecorate = () -> new h1("decorated text!");
                                }};
                            }};
                        }
                    }""",
                """
                    package js;
                                       
                    import org.over64.jscripter.StdTypeConversion;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                       
                    class Demo implements Component0<div> {
                       \s
                        Demo() {
                            super();
                        }
                       \s
                        @Override
                        public div mount() {
                            final com.over64.greact.dom.HTMLNativeElements.div $root = (com.over64.greact.dom.HTMLNativeElements.div)com.over64.greact.dom.Globals.gReactElement;
                            return com.over64.greact.dom.Globals.gReactReturn(()->{
                                final com.over64.greact.dom.HTMLNativeElements.div $el0 = com.over64.greact.dom.Globals.document.createElement("div");
                                final js.Decorator $comp0 = new Decorator();
                                {
                                    $comp0.forDecorate = ()->{
                                        final com.over64.greact.dom.HTMLNativeElements.h1 $root = (com.over64.greact.dom.HTMLNativeElements.h1)com.over64.greact.dom.Globals.gReactElement;
                                        return com.over64.greact.dom.Globals.gReactReturn(()->{
                                            $root.innerText = "decorated text!";
                                        });
                                    };
                                }
                                com.over64.greact.dom.Globals.gReactMount($el0, $comp0);
                                $root.appendChild($el0);
                            });
                        }
                    }"""));
    }
}
