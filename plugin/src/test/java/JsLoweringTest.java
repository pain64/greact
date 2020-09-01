import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import util.TestCompiler;

import java.io.*;

public class JsLoweringTest {

    void assertCompiled(String javaSrc, String jsDest) throws IOException {
        Assertions.assertEquals(jsDest,
            TestCompiler.compile("js.Test", javaSrc).values().iterator().next().getCharContent(true));
    }

    @Test
    void emptyClass() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {}""",
            """
                class js$Test {
                }""");
    }

    @Test
    void methodDeclare() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  static void bar() {}
                  void foo(int x, String y) {}
                }""",
            """
                class js$Test {
                  static bar() {
                  }
                  
                  foo(x, y) {
                  }
                }""");
    }

    @Test
    void methodWithReturn() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  int baz() { return 42; }
                }""",
            """
                class js$Test {
                  baz() {
                    return 42;
                  }
                }""");
    }
}