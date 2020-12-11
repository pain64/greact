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
                    import com.over64.greact.model.components.Component;
                                        
                    class Demo implements Component<div> {
                      int[] list = new int[]{1, 2, 3};
                      
                      @Override public void mount() {              
                        render(new div() {{
                          new ul() {{
                            dependsOn = list;
                            for (var x : list)
                              new li() {{ new a("text:" + x); }};
                          }};
                         
                          new button("do effect") {{
                            onclick = () -> effect(list);
                          }};
                        }});
                      }
                    }""",
                """
                    package js;
                       
                    import org.over64.jscripter.StdTypeConversion;
                    import com.over64.greact.GReact;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                    import com.over64.greact.model.components.Component;
                    import com.over64.greact.dom.HtmlElement;
                   
                    class Demo implements Component {
                       \s
                        Demo() {
                            super();
                        }
                       \s
                        @Override
                        public void mount(HtmlElement dom) {
                            {
                                final com.over64.greact.dom.DocumentFragment $frag = com.over64.greact.dom.Globals.document.createDocumentFragment();
                                final com.over64.greact.dom.HTMLNativeElements.div $el0 = com.over64.greact.dom.Globals.document.createElement("div");
                                {
                                    $el0.className = "my-div";
                                    $el0.fake.className = "123";
                                    {
                                        final com.over64.greact.dom.HTMLNativeElements.h1 $el1 = com.over64.greact.dom.Globals.document.createElement("h1");
                                        {
                                            $el1.innerText = "hello, GReact";
                                        }
                                        $frag.appendChild($el1);
                                    }
                                }
                                $frag.appendChild($el0);
                                dom.appendChild($frag);
                            }
                        }
                    }"""));


    }
}
