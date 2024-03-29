package lowering;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;

public class _06ShimJavaAPI {

    @Test void staticCall() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  int x = Integer.valueOf("42");
                }""",
            """
                class js_Test {
                  constructor() {
                    const __init__ = () => {
                      this.x = std_java_lang_Integer._valueOf('42');
                    };
                    __init__();
                  }
                }
                """);
    }

    @Test void asStaticCall() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  int x = "x".compareTo("y");
                }""",
            """
                class js_Test {
                  constructor() {
                    const __init__ = () => {
                      this.x = std_java_lang_String._compareTo.call('x', 'y');
                    };
                    __init__();
                  }
                }
                """);
    }

    @Test void jsExpression() throws IOException {
        assertCompiled(
            """
                package js;
                import jstack.jscripter.transpiler.model.JSExpression;
                public class Test {
                  int x = JSExpression.of("1 + 1");
                }""",
            """
                class js_Test {
                  constructor() {
                    const __init__ = () => {
                      this.x = 1 + 1;
                    };
                    __init__();
                  }
                }
                """);
    }

    @Test void jsExpressionMultiline() throws IOException {
        assertCompiled(
            """
                package js;
                import jstack.jscripter.transpiler.model.JSExpression;
                public class Test {
                  int x = JSExpression.of(""\"
                     1 
                        + 1""\");
                }""",
            """
                class js_Test {
                  constructor() {
                    const __init__ = () => {
                      this.x = 1
                   + 1;
                    };
                    __init__();
                  }
                }
                """);
    }

    @Test void jsExpressionCheckThatNoCollisionWithMethodNamed__of__() throws IOException {
        assertCompiled(
            """
                package js;
                import jstack.jscripter.transpiler.model.JSExpression;
                public class Test {
                  static int of() { return 42; }
                  int x = of();
                }""",
            """
                class js_Test {
                  constructor() {
                    const __init__ = () => {
                      this.x = this.constructor._of();
                    };
                    __init__();
                  }
                  static _of() {
                    return 42;
                  }
                }
                """);
    }
}
