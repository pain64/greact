package modifier.mount;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

public class ComponentNonNativeElement {
    @Test void testForReturn() throws IOException {
        CompileAssert.assertCompiledMany(
            new CompileAssert.CompileCase("js.Demo",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    class Demo implements Component0<h1> {
                      static class Child implements Component0<h1> {
                        @Override public h1 mount() { return new h1("hello"); }
                      }
                      
                      @Override public Child mount() { return new Child(); }
                    }""",
                """
                    package js;
                                        
                    import org.over64.jscripter.StdTypeConversion;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    class Demo implements Component0<h1> {
                       \s
                        Demo() {
                            super();
                        }
                       \s
                        static class Child implements Component0<h1> {
                           \s
                            Child() {
                                super();
                            }
                           \s
                            @Override
                            public h1 mount() {
                                final com.over64.greact.dom.HTMLNativeElements.h1 $root = (com.over64.greact.dom.HTMLNativeElements.h1)com.over64.greact.dom.Globals.gReactElement;
                                return com.over64.greact.dom.Globals.gReactReturn(()->{
                                    $root.innerText = "hello";
                                });
                            }
                        }
                       \s
                        @Override
                        public Child mount() {
                            final com.over64.greact.dom.HTMLNativeElements.h1 $root = (com.over64.greact.dom.HTMLNativeElements.h1)com.over64.greact.dom.Globals.gReactElement;
                            return com.over64.greact.dom.Globals.gReactReturn(()->com.over64.greact.dom.Globals.gReactMount($root, new Child(), new Object[]{}));
                        }
                    }"""));

    }
}
