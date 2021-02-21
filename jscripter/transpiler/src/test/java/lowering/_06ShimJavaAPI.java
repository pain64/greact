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
                  __init__() {
                    this.x = std$java$lang$Integer.valueOf('42')
                  }
                  constructor() {
                    super();
                    this.__init__();
                  }
                }""");
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
                  __init__() {
                    this.x = std$java$lang$String.compareTo.call('x', 'y')
                  }
                  constructor() {
                    super();
                    this.__init__();
                  }
                }""");
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
                  __init__() {
                    this.x = 1 + 1
                  }
                  constructor() {
                    super();
                    this.__init__();
                  }
                }""");
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
                  __init__() {
                    this.x = 1
                   + 1
                  }
                  constructor() {
                    super();
                    this.__init__();
                  }
                }""");
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
                  __init__() {
                    this.x = this.constructor.of()
                  }
                  constructor() {
                    super();
                    this.__init__();
                  }
                  
                  static of() {
                    return 42
                  }
                }""");
    }
}
