package lowering;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;
import static util.CompileAssert.assertCompiledMany;

public class _10AnonInnerClass {
    @Test void simpleDeclaration() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    public class A {
                    }""",
                """
                    class js_A extends Object {
                      constructor() {
                        super();
                      }
                    }
                    """),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    class B {
                      void foo() {
                        new A() {
                        };
                      }
                    }""",
                """
                    class js_B extends Object {
                      constructor() {
                        super();
                      }
                      _foo() {
                        (this0 => {
                          return new class extends js_A {
                            constructor() {
                              super();
                            }
                          }()
                        })(this);
                      }
                    }
                    """));
    }

    @Test void withClosure() throws IOException {
        assertCompiled(
            """
                package js;
                class Test {
                  static class A {
                    int y;
                  }
                  int x = 42;
                  void foo() {
                    new A() {{
                      y = x;
                      var z = x;
                    }};
                  }
                }
                """,
            """
                class js_Test extends Object {
                  constructor() {
                    const __init__ = () => {
                      this.x = 42;
                    };
                    super();
                    __init__();
                  }
                  static A = class extends Object {
                    constructor() {
                      const __init__ = () => {
                        this.y = 0;
                      };
                      super();
                      __init__();
                    }
                  }
                  _foo() {
                    (this0 => {
                      return new class extends js_Test_A {
                        constructor() {
                          const __init__ = () => {
                            this.y = this0.x;
                            const z = this0.x;
                          };
                          super();
                          __init__();
                        }
                      }()
                    })(this);
                  }
                }
                """);
    }
}
