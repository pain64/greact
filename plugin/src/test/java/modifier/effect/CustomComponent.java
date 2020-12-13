package modifier.effect;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

public class CustomComponent {
    @Test void customComponent() throws IOException {
        CompileAssert.assertCompiledMany(
            new CompileAssert.CompileCase("js.Child",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                    import com.over64.greact.model.components.Component;
                                        
                    class Child implements Component<h1> {
                      public int mainAnswer;
                      @Override public void mount() {              
                        render(new h1("a child"));
                      }
                    }""",
                """
                    package js;
                                        
                    import org.over64.jscripter.StdTypeConversion;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                    import com.over64.greact.model.components.Component;
                                        
                    class Child implements Component<h1> {
                       \s
                        Child() {
                            super();
                        }
                        public int mainAnswer;
                       \s
                        @Override
                        public void mount() {
                            final com.over64.greact.dom.HTMLNativeElements.h1 $root = (com.over64.greact.dom.HTMLNativeElements.h1)com.over64.greact.dom.Globals.gReactElement;
                            {
                                $root.innerText = "a child";
                            }
                        }
                    }"""),
            new CompileAssert.CompileCase("js.Demo",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                    import com.over64.greact.model.components.Component;
                                        
                    class Demo implements Component<div> {
                      @Override public void mount() {              
                        render(new div() {{
                          new Child() {{
                            mainAnswer = 42;
                          }};
                        }});
                      }
                    }""",
                """
                   """));
    }
}
