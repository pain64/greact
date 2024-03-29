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
                    import static util.TestServer.server;
                    import jstack.jscripter.transpiler.model.Async;
                    class Simple {
                      @Async void simple() {
                        int x = server(none -> 42);
                      }
                    }""",
                """
                    package js;

                    import static util.TestServer.server;
                    import jstack.jscripter.transpiler.model.Async;

                    class Simple {
                       \s
                        @jstack.jscripter.transpiler.model.RPCEndPoint
                        @jstack.jscripter.transpiler.model.DoNotTranspile
                        public static java.lang.Object $endpoint_line6_col13(util.TestServer.DI x0, com.fasterxml.jackson.databind.ObjectMapper x1, java.util.List<com.fasterxml.jackson.databind.JsonNode> x2) {
                            return 42;
                        }
                       \s
                        Simple() {
                            super();
                        }
                       \s
                        @Async
                        void simple() {
                            int x = jstack.greact.dom.Globals.doRemoteCall("/util.TestServer", "js.Simple.$endpoint_line6_col13");
                        }
                    }"""));

    }

    @Test void methodReference() throws IOException {
        CompileAssert.assertCompiledMany(
            new CompileAssert.CompileCase("js.Simple",
                """
                    package js;
                    import static util.TestServer.server;
                    import static util.TestServer.DI;
                    import jstack.jscripter.transpiler.model.Async;
                    class Simple {
                      @Async void simple() {
                        int x = server(DI::method);
                      }
                    }""",
                """
                    package js;

                    import static util.TestServer.server;
                    import static util.TestServer.DI;
                    import jstack.jscripter.transpiler.model.Async;

                    class Simple {
                       \s
                        @jstack.jscripter.transpiler.model.RPCEndPoint
                        @jstack.jscripter.transpiler.model.DoNotTranspile
                        public static java.lang.Object $endpoint_line7_col13(util.TestServer.DI x0, com.fasterxml.jackson.databind.ObjectMapper x1, java.util.List<com.fasterxml.jackson.databind.JsonNode> x2) {
                            return x0.method();
                        }
                       \s
                        Simple() {
                            super();
                        }
                       \s
                        @Async
                        void simple() {
                            int x = jstack.greact.dom.Globals.doRemoteCall("/util.TestServer", "js.Simple.$endpoint_line7_col13");
                        }
                    }"""));

    }
}
