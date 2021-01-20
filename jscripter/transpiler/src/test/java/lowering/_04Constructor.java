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
                class js$Test extends Object {
                  constructor($over, x, y) {
                    switch($over) {
                      case 1:
                        super();
                        this.z = 42
                        this.f = 0
                        this.x = x;
                        this.y = y;
                        break
                      case 2:
                        super();
                        this.z = 42
                        this.f = 0
                        this.x = x;
                        this.y = 'hello';
                        break
                    }
                  }
                }""");
    }

    @Test
    void constructorOverloaded() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    public class A {
                      A(int x){}
                      A(float x){}
                    }""",
                """
                    class js$A extends Object {
                      constructor($over, x) {
                        switch($over) {
                          case 1:
                            super();
                            break
                          case 2:
                            super();
                            break
                        }
                      }
                    }"""),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    public class B {
                      B() { new A(42); }
                    }""",
                """
                    class js$B extends Object {
                      constructor() {
                        super();
                        new js$A(1, 42);
                      }
                    }""")
        );
    }
}
