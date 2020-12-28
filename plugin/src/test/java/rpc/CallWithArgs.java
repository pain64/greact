package rpc;

import org.junit.jupiter.api.Test;
import util.CompileAssert;
import java.io.IOException;
import java.util.function.Consumer;

public class CallWithArgs {
    @Test
    void simpleCall() throws IOException {
        CompileAssert.assertCompiledMany(
            new CompileAssert.CompileCase("js.Simple",
                """
                    package js;
                    import com.greact.model.async;
                    import java.util.ArrayList;
                    import static util.TestServer.server;
                    class Simple {
                      int x = 40;
                      @async void simple() {
                       var list = new ArrayList<String>();
                        list.add("Hello");
                        var ff = list.get(0);
                        
                        var y = 1;
                        
                       // java.util.function.Consumer<Integer> c = e -> {
                          server(none -> {
                            var z = x + y;// + e;
                            return z + 1;
                          });
                       // };
                      }
                    }""",
                """
                    package js;
                                       
                    import org.over64.jscripter.StdTypeConversion;
                    import com.greact.model.async;
                    import static util.TestServer.server;
                                       
                    class Simple {
                       \s
                        public static java.lang.Object $endpoint0(java.lang.Void x0, com.google.gson.Gson x1, java.util.List<com.google.gson.JsonElement> x2) {
                            return 42;
                        }
                       \s
                        Simple() {
                            super();
                        }
                       \s
                        @async
                        void simple() {
                            int x = com.over64.greact.dom.Globals.doRemoteCall("/rpc", "js.Simple.$endpoint0", new Object[]{});
                        }
                    }"""));

    }
}
