package rpc;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

public class CallNoArgs {
    @Test void simpleCall() throws IOException {
        CompileAssert.assertCompiledMany(
            new CompileAssert.CompileCase("js.Simple",
                """
                    package js;
                    import com.greact.model.async;
                    import static util.TestServer.server;
                    class Simple {
                      @async void simple() {
                        int x = server(none -> 42);
                      }
                    }""",
                """
                        package js;
                                            
                        import com.greact.model.async;
                        import static util.TestServer.server;
                                            
                        class Simple {
                           \s
                            @com.greact.model.DoNotTranspile
                            public static java.lang.Object $endpoint0(java.lang.Void x0, com.fasterxml.jackson.databind.ObjectMapper x1, java.util.List<com.fasterxml.jackson.databind.JsonNode> x2) {
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
