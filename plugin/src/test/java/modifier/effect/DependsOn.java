package modifier.effect;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

public class DependsOn {
    @Test void dependsOn() throws IOException {
        CompileAssert.assertCompiledMany(
            new CompileAssert.CompileCase("js.Demo",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    class Demo implements Component0<div> {
                      int[] list = new int[]{1, 2, 3};
                      
                      @Override public div mount() {              
                        return new div() {{
                          new ul() {{
                            dependsOn = list;
                            for (var x : list)
                              new li() {{ new a("text:" + x); }};
                          }};
                         
                          new button("do effect") {{
                            onclick = ev -> effect(list);
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
                        int[] list = new int[]{1, 2, 3};
                       \s
                        @Override
                        public div mount() {
                            final com.over64.greact.dom.HTMLNativeElements.div $root = (com.over64.greact.dom.HTMLNativeElements.div)com.over64.greact.dom.Globals.gReactElement;
                            return com.over64.greact.dom.Globals.gReactReturn(()->{
                                ($viewFrag0 = com.over64.greact.dom.Fragment.of(()->{
                                    $viewFrag0.cleanup();
                                    final com.over64.greact.dom.HTMLNativeElements.ul $el0 = com.over64.greact.dom.Globals.document.createElement("ul");
                                    {
                                        $el0.dependsOn = list;
                                        for (int x : list) {
                                            final com.over64.greact.dom.HTMLNativeElements.li $el1 = com.over64.greact.dom.Globals.document.createElement("li");
                                            {
                                                final com.over64.greact.dom.HTMLNativeElements.a $el2 = com.over64.greact.dom.Globals.document.createElement("a");
                                                {
                                                    $el2.innerText = "text:" + x;
                                                }
                                                $el1.appendChild($el2);
                                            }
                                            $el0.appendChild($el1);
                                        }
                                    }
                                    $viewFrag0.appendChild($el0);
                                }, $root)).renderer.render();
                                final com.over64.greact.dom.HTMLNativeElements.button $el3 = com.over64.greact.dom.Globals.document.createElement("button");
                                {
                                    $el3.innerText = "do effect";
                                    $el3.onclick = (ev)->effect$list(list);
                                }
                                $root.appendChild($el3);
                            });
                        }
                        private com.over64.greact.dom.Fragment $viewFrag0;
                       \s
                        private void effect$list(java.lang.Object x0) {
                            $viewFrag0.renderer.render();
                        }
                    }"""));


    }
}
