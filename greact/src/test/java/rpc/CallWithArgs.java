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
                    import jstack.jscripter.transpiler.model.Async;
                    import static util.TestServer.server;
                    class Simple {
                      int x = 40;
                      @Async void simple() {
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

                    import jstack.jscripter.transpiler.model.Async;
                    import static util.TestServer.server;

                    class Simple {
                       \s
                        @jstack.jscripter.transpiler.model.RPCEndPoint
                        @jstack.jscripter.transpiler.model.DoNotTranspile
                        public static java.lang.Object $endpoint_line10_col7(util.TestServer.DI x0, com.fasterxml.jackson.databind.ObjectMapper x1, java.util.List<com.fasterxml.jackson.databind.JsonNode> x2) {
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
                        @Async
                        void simple() {
                            int y = 1;
                            jstack.greact.dom.Globals.doRemoteCall("/util.TestServer", "js.Simple.$endpoint_line10_col7", x, y);
                        }
                    }"""));

    }

    @Test
    void accessVariableFiveTimes() throws IOException {
        CompileAssert.assertCompiledMany(
            new CompileAssert.CompileCase("js.Simple",
                """
                    package js;
                    import jstack.jscripter.transpiler.model.Async;
                    import static util.TestServer.server;
                    class Simple {
                      int x = 40;
                      @Async void simple() {
                        var y = 1;

                          server(none -> {
                            var z = y + y + y + y + y;
                            return z + 1;
                          });
                      }
                    }""",
                """
                    package js;

                    import jstack.jscripter.transpiler.model.Async;
                    import static util.TestServer.server;

                    class Simple {
                       \s
                        @jstack.jscripter.transpiler.model.RPCEndPoint
                        @jstack.jscripter.transpiler.model.DoNotTranspile
                        public static java.lang.Object $endpoint_line9_col7(util.TestServer.DI x0, com.fasterxml.jackson.databind.ObjectMapper x1, java.util.List<com.fasterxml.jackson.databind.JsonNode> x2) {
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
                        @Async
                        void simple() {
                            int y = 1;
                            jstack.greact.dom.Globals.doRemoteCall("/util.TestServer", "js.Simple.$endpoint_line9_col7", y);
                        }
                    }"""));

    }
}
