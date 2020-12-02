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
                    super();
                    this.x = std$java$lang$Integer.valueOf('42')
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
                  constructor() {
                    super();
                    this.x = std$java$lang$String.compareTo.call('x', 'y')
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
                  constructor() {
                    super();
                    this.x = 1 + 1
                  }
                }""");
    }
}
