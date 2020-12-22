package modifier.effect;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

public class CustomComponent {
    @Test void customComponent0() throws IOException {
        CompileAssert.assertCompiledMany(
            new CompileAssert.CompileCase("js.Child",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    class Child implements Component0<h1> {
                      public int mainAnswer;
                      @Override public h1 mount() {              
                        return new h1("a child");
                      }
                    }""",
                """
                    package js;
                                       
                    import org.over64.jscripter.StdTypeConversion;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                       
                    class Child implements Component0<h1> {
                       \s
                        Child() {
                            super();
                        }
                        public int mainAnswer;
                       \s
                        @Override
                        public h1 mount() {
                            final com.over64.greact.dom.HTMLNativeElements.h1 $root = (com.over64.greact.dom.HTMLNativeElements.h1)com.over64.greact.dom.Globals.gReactElement;
                            return com.over64.greact.dom.Globals.gReactReturn(()->{
                                $root.innerText = "a child";
                            });
                        }
                    }"""),
            new CompileAssert.CompileCase("js.Demo",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    class Demo implements Component0<div> {
                      @Override public div mount() {              
                        return new div() {{
                          new Child() {{
                            mainAnswer = 42;
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
                                final com.over64.greact.dom.HTMLNativeElements.h1 $el0 = com.over64.greact.dom.Globals.document.createElement("h1");
                                final js.Child $comp0 = new Child();
                                {
                                    $comp0.mainAnswer = 42;
                                }
                                com.over64.greact.dom.Globals.gReactMount($el0, $comp0);
                                $root.appendChild($el0);
                            });
                        }
                    }"""));
    }
}
