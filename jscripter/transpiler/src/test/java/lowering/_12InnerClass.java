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
                class js$Test extends Object {
                  static A = class extends Object {
                    constructor() {
                      let __init__ = () => {
                        this.x = 42
                        this.y = 15
                      };
                      super();
                      __init__();
                    }
                                
                    add() {
                      return this.x + this.y
                    }
                  }
                    
                  constructor() {
                    super();
                  }
                }""");
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
                class js$Test extends Object {
                  static A = class extends Object {
                    constructor(x, y) {
                      let __init__ = () => {
                        this.x = 0
                        this.y = 0
                      };
                      super();
                      __init__();
                      this.x = x;
                      this.y = y;
                    }
                  }
                    
                  constructor() {
                    super();
                  }
                }""");
    }

    @Test
    void newInstanceCreate() throws IOException {
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
                    class js$A extends Object {
                      static B = class extends Object {
                        constructor() {
                          super();
                        }
                      }
                        
                      constructor() {
                        super();
                      }
                    }"""),
            new CompileAssert.CompileCase("js.C",
                """
                    package js;
                    import static js.A.B;
                    class C {
                      B b = new B();
                    }
                    """,
                """
                    class js$C extends Object {
                      constructor() {
                        let __init__ = () => {
                          this.b = new js$A.B()
                        };
                        super();
                        __init__();
                      }
                    }"""));
    }
}
