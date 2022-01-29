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
                class js$Test extends Object {
                  constructor() {
                    const __init__ = () => {
                      this.x = std$java$lang$Integer._valueOf('42');
                    };
                    super();
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
                class js$Test extends Object {
                  constructor() {
                    const __init__ = () => {
                      this.x = std$java$lang$String._compareTo.call('x', 'y');
                    };
                    super();
                    __init__();
                  }
                }
                """);
    }

    @Test void jsExpression() throws IOException {
        assertCompiled(
            """
                package js;
                import com.greact.model.JSExpression;
                public class Test {
                  int x = JSExpression.of("1 + 1");
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    const __init__ = () => {
                      this.x = 1 + 1;
                    };
                    super();
                    __init__();
                  }
                }
                """);
    }

    @Test void jsExpressionMultiline() throws IOException {
        assertCompiled(
            """
                package js;
                import com.greact.model.JSExpression;
                public class Test {
                  int x = JSExpression.of(""\"
                     1 
                        + 1""\");
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    const __init__ = () => {
                      this.x = 1
                   + 1;
                    };
                    super();
                    __init__();
                  }
                }
                """);
    }

    @Test void jsExpressionCheckThatNoCollisionWithMethodNamed__of__() throws IOException {
        assertCompiled(
            """
                package js;
                import com.greact.model.JSExpression;
                public class Test {
                  static int of() { return 42; }
                  int x = of();
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    const __init__ = () => {
                      this.x = this.constructor._of();
                    };
                    super();
                    __init__();
                  }
                  static _of() {
                    return 42;
                  }
                }
                """);
    }
}
