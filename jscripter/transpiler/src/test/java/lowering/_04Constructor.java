package lowering;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;
import static util.CompileAssert.assertCompiledMany;

public class _04Constructor {

    @Test
    void constructorSimple() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  final String x;
                  final String y;
                  int z = 42;
                  int f; // default value is 0
                  
                  public Test(String x, String y) {
                    this.x = x;
                    this.y = y;
                  }
                  
                  public Test(String x) {
                    this.x = x;
                    this.y = "hello";
                  }
                }""",
            """
                class js_Test {
                  constructor($over, ...__args) {
                    const __init__ = () => {
                      this.x = null;
                      this.y = null;
                      this.z = 42;
                      this.f = 0;
                    };
                    if($over === 1) {
                      const [x, y] = __args;
                      __init__();
                      this.x = x;
                      this.y = y;
                    } else if($over === 2) {
                      const [x] = __args;
                      __init__();
                      this.x = x;
                      this.y = 'hello';
                    }
                  }
                }
                """);
    }

    @Test
    void initBlock() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  final String x;
                  final String y;
                  
                  {
                    x = "hello";
                  }
                  
                  public Test(String y) {
                    this.y = y;
                  }
                }""",
            """
                class js_Test {
                  constructor(y) {
                    const __init__ = () => {
                      this.x = null;
                      this.y = null;
                      this.x = 'hello';
                    };
                    __init__();
                    this.y = y;
                  }
                }
                """);
    }

    @Test void constructorOverloaded() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    public class A {
                      A(int x, int y){
                        var some = x + y;
                      }
                      A(float z){
                        var some = z;
                      }
                    }""",
                """
                    class js_A {
                      constructor($over, ...__args) {
                        if($over === 1) {
                          const [x, y] = __args;
                          const some = x + y;
                        } else if($over === 2) {
                          const [z] = __args;
                          const some = z;
                        }
                      }
                    }
                    """),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    public class B {
                      B() { new A(42); }
                    }""",
                """
                    class js_B {
                      constructor() {
                        new js_A(2, 42);
                      }
                    }
                    """)
        );
    }
}