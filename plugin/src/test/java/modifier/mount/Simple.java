package modifier.mount;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

public class Simple {
    @Test void foo() throws IOException {
        CompileAssert.assertCompiledMany(
            new CompileAssert.CompileCase("js.Demo",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    class Demo implements Component0<div> {
                      int nUsers = 1;
                      
                      @Override public div mount() {                        
                        return new div() {{
                           new h1("GReact users: " + nUsers);
                           new button("increment") {{
                             onclick = ev -> effect(nUsers += 1);
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
                        int nUsers = 1;
                       \s
                        @Override
                        public div mount() {
                            final com.over64.greact.dom.HTMLNativeElements.div $root = (com.over64.greact.dom.HTMLNativeElements.div)com.over64.greact.dom.Globals.gReactElement;
                            return com.over64.greact.dom.Globals.gReactReturn(()->{
                                final com.over64.greact.dom.HTMLNativeElements.h1 $el0 = com.over64.greact.dom.Globals.document.createElement("h1");
                                {
                                    ($viewFrag0 = com.over64.greact.dom.Fragment.of(()->{
                                        $viewFrag0.cleanup();
                                        $el0.innerText = "GReact users: " + nUsers;
                                    }, $el0)).renderer.render();
                                }
                                $root.appendChild($el0);
                                final com.over64.greact.dom.HTMLNativeElements.button $el1 = com.over64.greact.dom.Globals.document.createElement("button");
                                {
                                    $el1.innerText = "increment";
                                    $el1.onclick = (ev)->effect$nUsers(nUsers += 1);
                                }
                                $root.appendChild($el1);
                            });
                        }
                        private com.over64.greact.dom.Fragment $viewFrag0;
                       \s
                        private void effect$nUsers(java.lang.Object x0) {
                            $viewFrag0.renderer.render();
                        }
                    }"""));

    }
}
