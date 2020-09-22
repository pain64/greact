package lowering;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;

public class _06ShimJavaAPI {

    @Test void test() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  int x = Integer.valueOf("42");
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    super()
                    this.x = com$greact$shim$java$lang$Integer.valueOf('42')
                  }
                }""");
    }
}
