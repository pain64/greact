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

                    import com.greact.model.async;
                    import static util.TestServer.server;

                    class Simple {
                       \s
                        @com.over64.greact.rpc.RPC.RPCEndPoint
                        public static java.lang.Object $endpoint0(java.lang.Void x0, com.fasterxml.jackson.databind.ObjectMapper x1, java.util.List<com.fasterxml.jackson.databind.JsonNode> x2) {
                            final int $closure1 = x1.treeToValue(x2.get(1), java.lang.Integer.class);
                            final int $closure0 = x1.treeToValue(x2.get(0), java.lang.Integer.class);
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
                            com.over64.greact.dom.Globals.doRemoteCall("/rpc", "js.Simple.$endpoint0", x, y);
                        }
                    }"""));

    }

    @Test
    void accessVariableFiveTimes() throws IOException {
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

                          server(none -> {
                            var z = y + y + y + y + y;
                            return z + 1;
                          });
                      }
                    }""",
                """
                    package js;

                    import com.greact.model.async;
                    import static util.TestServer.server;

                    class Simple {
                       \s
                        @com.over64.greact.rpc.RPC.RPCEndPoint
                        public static java.lang.Object $endpoint0(java.lang.Void x0, com.fasterxml.jackson.databind.ObjectMapper x1, java.util.List<com.fasterxml.jackson.databind.JsonNode> x2) {
                            final int $closure0 = x1.treeToValue(x2.get(0), java.lang.Integer.class);
                            int z = $closure0 + $closure0 + $closure0 + $closure0 + $closure0;
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
                            com.over64.greact.dom.Globals.doRemoteCall("/rpc", "js.Simple.$endpoint0", y);
                        }
                    }"""));

    }
}
