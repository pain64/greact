package modifier.mount;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

public class HelloWorld {
    @Test void helloWorld() throws IOException {
        CompileAssert.assertCompiledMany(
            new CompileAssert.CompileCase("js.Demo",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    class Demo implements Component0<h1> {
                      int nUsers = 1;
                      
                      @Override public h1 mount() {                        
                        return new h1("hello, world");
                      }
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
                        int nUsers = 1;
                       \s
                        @Override
                        public h1 mount() {
                            final com.over64.greact.dom.HTMLNativeElements.h1 $root = (com.over64.greact.dom.HTMLNativeElements.h1)com.over64.greact.dom.Globals.gReactElement;
                            return com.over64.greact.dom.Globals.gReactReturn(()->{
                                $root.innerText = "hello, world";
                            });
                        }
                    }"""));

    }
}
