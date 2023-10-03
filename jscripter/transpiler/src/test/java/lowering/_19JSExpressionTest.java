package lowering;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static util.CompileAssert.assertCompiled;

public class _19JSExpressionTest {
    @Test
    void simpleJsExpressionTest() throws IOException {
        assertCompiled(
            """
                package js;
                import jstack.jscripter.transpiler.model.JSExpression;
                                
                public class Test {
                  void baz() {
                    var a = JSExpression.of("'some expr'");
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _baz() {
                    const a = 'some expr';
                  }
                }
                """);
    }

    @Test
    void jsExpressionTestWithArgs() throws IOException {
        assertCompiled(
            """
                package js;
                import jstack.jscripter.transpiler.model.JSExpression;
                                
                public class Test {
                  void baz() {
                    var arg1 = 1;
                    var arg2 = 2;
                    var a = JSExpression.of(":1 + :2", arg1, arg2);
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _baz() {
                    const arg1 = 1;
                    const arg2 = 2;
                    const a = arg1 + arg2;
                  }
                }
                """);
    }

    @Test
    void jsExpressionTestWithEscaping() throws IOException {
        assertCompiled(
            """
                package js;
                import jstack.jscripter.transpiler.model.JSExpression;
                                
                public class Test {
                  void baz() {
                    var arg = 1;
                    var arg2 = 2;
                    var a = JSExpression.of(":1 + '\\\\:2'", arg, arg2);
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _baz() {
                    const arg = 1;
                    const arg2 = 2;
                    const a = arg + ':2';
                  }
                }
                """);
    }

    @Test
    void jsExpressionTestWithSelect() throws IOException {
        assertCompiled(
            """
                package js;
                import jstack.jscripter.transpiler.model.JSExpression;
                                
                public class Test {
                  record Data(int a){};
                  record Data1(Data data){};
                  void baz() {
                    var arg = new Data(1);
                    var arg2 = new Data1(arg);
                    var a = JSExpression.of(":1 + :2", arg.a, arg2.data.a);
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  static Data = class {
                    constructor(a) {
                      this.a = a;
                    }
                  }
                  static Data1 = class {
                    constructor(data) {
                      this.data = data;
                    }
                  }
                  _baz() {
                    const arg = new js_Test.Data(1);
                    const arg2 = new js_Test.Data1(arg);
                    const a = arg.a + arg2.data.a;
                  }
                }
                """);
    }

    @Test
    void jsExpressionTestWithMethodReference() throws IOException {
        assertCompiled(
            """
                package js;
                import jstack.jscripter.transpiler.model.JSExpression;
                
                public class Test {
                  void f() {}
                  static void f2() {}
                  void baz() {
                    var a = JSExpression.of(":1() + :2()", (java.lang.Runnable) Test::f2, (java.lang.Runnable) this::f);
                  }
                }
                """,
            """
                class js_Test {
                  constructor() {
                  }
                  _f() {
                  }
                  static _f2() {
                  }
                  _baz() {
                    const a = js_Test._f2() + this._f();
                  }
                }
                """);
    }

    @Test
    void jsExpressionTestWithCast() throws IOException {
        assertCompiled(
            """
                package js;
                import jstack.jscripter.transpiler.model.JSExpression;
                
                public class Test {
                  record Data(int a){};
                  void baz() {
                    var dataArray = new Data[]{new Data(1)};
                    var a = JSExpression.of(":1", (java.lang.Object) dataArray);
                  }
                }
                """,
            """
                class js_Test {
                  constructor() {
                  }
                  static Data = class {
                    constructor(a) {
                      this.a = a;
                    }
                  }
                  _baz() {
                    const dataArray = [new js_Test.Data(1)];
                    const a = dataArray;
                  }
                }
                """);
    }
}
