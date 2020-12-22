package modifier.effect;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

public class Simple {
    @Test void simplestEffect() throws IOException {
        CompileAssert.assertCompiledMany(
            new CompileAssert.CompileCase("js.Demo",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    class Demo implements Component0<div> {
                      int nUsers = 1;
                      
                      @Override public div mount() {              
                        return new div() {{
                          new h1() {{ innerText = "GReact users: " + nUsers; }};
                          
                          if(nUsers > 100)
                            new h1() {{ innerText = "too much users: " + nUsers; }};
                            
                          new button() {{
                            innerText = "increment";
                            onclick = () -> {
                              effect(nUsers += 1);
                            };
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
                                ($viewFrag0 = com.over64.greact.dom.Fragment.of(()->{
                                    $viewFrag0.cleanup();
                                    final com.over64.greact.dom.HTMLNativeElements.h1 $el0 = com.over64.greact.dom.Globals.document.createElement("h1");
                                    {
                                        $el0.innerText = "GReact users: " + nUsers;
                                    }
                                    $viewFrag0.appendChild($el0);
                                }, $root)).renderer.render();
                                ($viewFrag1 = com.over64.greact.dom.Fragment.of(()->{
                                    $viewFrag1.cleanup();
                                    if (nUsers > 100) {
                                        final com.over64.greact.dom.HTMLNativeElements.h1 $el1 = com.over64.greact.dom.Globals.document.createElement("h1");
                                        {
                                            $el1.innerText = "too much users: " + nUsers;
                                        }
                                        $viewFrag1.appendChild($el1);
                                    }
                                }, $root)).renderer.render();
                                final com.over64.greact.dom.HTMLNativeElements.button $el2 = com.over64.greact.dom.Globals.document.createElement("button");
                                {
                                    $el2.innerText = "increment";
                                    $el2.onclick = ()->{
                                        effect$nUsers(nUsers += 1);
                                    };
                                }
                                $root.appendChild($el2);
                            });
                        }
                        private com.over64.greact.dom.Fragment $viewFrag0;
                        private com.over64.greact.dom.Fragment $viewFrag1;
                       \s
                        private void effect$nUsers(java.lang.Object x0) {
                            $viewFrag0.renderer.render();
                            $viewFrag1.renderer.render();
                        }
                    }"""));

    }
}
