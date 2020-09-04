package lowering;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.CompileAssert.*;

public class _00DeclarationsTest {

    @Test
    void klass() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {}""",
            """
                class js$Test {
                }""");
    }

    @Test
    void method() throws IOException {
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
    void localVar() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void baz() {
                    String x;
                  }
                }""",
            """
                class js$Test {
                  baz() {
                    let x = null
                  }
                }""");
    }

//    @Test
//    void innerClasses() throws IOException {
//        // FIXME: expected 1 java input and 2 js output files
//        // TODO: move this to ordering test
//        assertCompiledMany(
//            new CompileCase(
//                "js.Test",
//                """
//                    package js;
//                    public class Test {
//                      void bar() {};
//                    }""",
//                """
//                    class js$Test {
//                      bar() {
//                      }
//                    }"""),
//            new CompileCase(
//                "js.Test2",
//                """
//                    package js;
//                    public class Test2 {
//                      void baz() {};
//                    }""",
//                """
//                    class js$Test2 {
//                      baz() {
//                      }
//                    }""")
//        );
//    }
}