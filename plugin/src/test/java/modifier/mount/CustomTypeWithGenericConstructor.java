package modifier.mount;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

import static util.CompileAssert.assertCompiledMany;

public class CustomTypeWithGenericConstructor {
    @Test void theTest() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                                   
                    import com.greact.model.JSExpression;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                    import com.over64.greact.dom.HtmlElement;
                                                  
                    class A<T> implements Component0<h1> {
                        public A(T[] data) {}                               
                        @Override
                        public h1 mount() {
                            return null;
                        }
                    }""",
                """
                    package js;
                                      
                    import org.over64.jscripter.StdTypeConversion;
                    import com.greact.model.JSExpression;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                    import com.over64.greact.dom.HtmlElement;
                                      
                    class A<T> implements Component0<h1> {
                       \s
                        public A(T[] data) {
                            super();
                        }
                       \s
                        @Override
                        public h1 mount() {
                            final com.over64.greact.dom.HTMLNativeElements.h1 $root = (com.over64.greact.dom.HTMLNativeElements.h1)com.over64.greact.dom.Globals.gReactElement;
                            return null;
                        }
                    }"""),
            new CompileAssert.CompileCase("js.Demo",
                """
                    package js;
                                   
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                   
                    public class Demo implements Component0<div> {                                                                     
                        @Override
                        public div mount() {
                            return new div() {{
                                new A<>(new String[] {}) {{
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
                            return com.over64.greact.dom.Globals.gReactReturn(()->{
                                final com.over64.greact.dom.HTMLNativeElements.h1 $el0 = com.over64.greact.dom.Globals.document.createElement("h1");
                                final js.Demo$1$1 $comp0 = new A<>(new String[]{}){
                                   \s
                                    (java.lang.String[] data) {
                                        super(data);
                                    }
                                    {
                                    }
                                };
                                com.over64.greact.dom.Globals.gReactMount($el0, $comp0, new Object[]{});
                                $root.appendChild($el0);
                            });
                        }
                    }"""));
    }
}
