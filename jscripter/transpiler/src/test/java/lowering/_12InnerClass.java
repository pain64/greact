package lowering;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;
import static util.CompileAssert.assertCompiledMany;

public class _12InnerClass {
    @Test void nestedClass() throws IOException {
        assertCompiled(
            """
                package js;
                class Test {
                  static class A {
                    int x = 42;
                    int y = 15;
                    
                    int add() { return x + y; }
                  }
                }
                """,
            """
                class js_Test {
                  constructor() {
                  }
                  static A = class {
                    constructor() {
                      const __init__ = () => {
                        this.x = 42;
                        this.y = 15;
                      };
                      __init__();
                    }
                    _add() {
                      return this.x + this.y;
                    }
                  }
                }
                """);
    }

    @Test void nestedRecord() throws IOException {
        assertCompiled(
            """
                package js;
                class Test {
                  record A(int x, int y) {}
                }
                """,
            """
                class js_Test {
                  constructor() {
                  }
                  static A = class {
                    constructor(x, y) {
                      this.x = x;
                      this.y = y;
                    }
                  }
                }
                """);
    }

    @Test void newInstanceCreate() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    class A {
                      static class B {
                      }
                    }
                    """,
                """
                    class js_A {
                      constructor() {
                      }
                      static B = class {
                        constructor() {
                        }
                      }
                    }
                    """),
            new CompileAssert.CompileCase("js.C",
                """
                    package js;
                    import static js.A.B;
                    class C {
                      B b = new B();
                    }
                    """,
                """
                    class js_C {
                      constructor() {
                        const __init__ = () => {
                          this.b = new js_A.B();
                        };
                        __init__();
                      }
                    }
                    """));
    }

    @Test void callStaticFromStaticInnerClass() throws IOException {
        assertCompiled(
            """
                package js;
                class Test {
                  static class A {
                    static int foo() { return 42; }
                  }
                  int x = A.foo();
                }
                """,
            """
                class js_Test {
                  constructor() {
                    const __init__ = () => {
                      this.x = js_Test.A._foo();
                    };
                    __init__();
                  }
                  static A = class {
                    constructor() {
                    }
                    static _foo() {
                      return 42;
                    }
                  }
                }
                """);
    }
}
