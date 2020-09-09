package lowering;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;

public class _03CallTest {

    @Test
    void callLocal() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void bar(int x) {}
                  void bar(long x) {}
                  
                  void baz() {
                    bar(42);
                    bar(42L);
                  }
                }""",
            """
                class js$Test {
                  bar$0(x) {
                  }
                  
                  bar$1(x) {
                  }
                  
                  baz() {
                    bar$0(42)
                    baz$1(42)
                  }
                }""");
    }
}
