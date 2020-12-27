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
                    class Simple {
                      void simple() {
                        int x = server(none -> 42);
                      }
                    }""",
                """
                  """));

    }
}
