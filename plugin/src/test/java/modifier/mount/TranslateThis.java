package modifier.mount;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

public class TranslateThis {
    @Test
    void translateThis() throws IOException {
        CompileAssert.assertCompiledMany(
            new CompileAssert.CompileCase("js.Demo",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    class Demo implements Component0<div> {
                      div local;
                      
                      @Override public div mount() {                        
                        return new div() {{
                          local = this;
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
                        div local;
                       \s
                        @Override
                        public div mount() {
                            final com.over64.greact.dom.HTMLNativeElements.div $root = (com.over64.greact.dom.HTMLNativeElements.div)com.over64.greact.dom.Globals.gReactElement;
                            return com.over64.greact.dom.Globals.gReactReturn(()->{
                                local = $root;
                            });
                        }
                    }"""));

    }
}
