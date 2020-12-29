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
                    import static util.TestServer.server;
                    class Simple {
                      int x = 40;
                      @async void simple() {                        
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
                        public static java.lang.Object $endpoint0(java.lang.Void x0, com.fasterxml.jackson.databind.ObjectMapper x1, java.util.List<com.fasterxml.jackson.databind.JsonNode> x2) {
                            final int $closure1 = x2.get(1).asInt();
                            final int $closure0 = x2.get(0).asInt();
                            int z = $closure0 + $closure1;
                            return z + 1;
                        }
                       \s
                        Simple() {
                            super();
                        }
                        int x = 40;
                       \s
                        @async
                        void simple() {
                            int y = 1;
                            com.over64.greact.dom.Globals.doRemoteCall("/rpc", "js.Simple.$endpoint0", new Object[]{x, y});
                        }
                    }"""));

    }
}
