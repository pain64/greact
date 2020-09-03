package lowering;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;

public class StmtTest {
    @Test
    void stmtReturn() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  int baz() { return 42; }
                }""",
            """
                class js$Test {
                  baz() {
                    return 42
                  }
                }""");
    }
}
