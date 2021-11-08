package lowering;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;

public class _09RecordDeclaration {
    @Test void simple() throws IOException {
        assertCompiled(
                """
                    package js;
                    record Test(int x, int y, int z) {}z
                    """,
                """
                    class js$Test extends Object {
                      constructor(x, y, z) {
                        let __init__ = () => {
                          this.x = 0
                          this.y = 0
                          this.z = 0
                        };
                        super();
                        __init__();
                        this.x = x;
                        this.y = y;
                        this.z = z;
                      }
                    }""");
    }

    @Test void accessProps() throws IOException {
        assertCompiled(
            """
                package js;
                class Test {
                    record A(int x, int y, int z) {}
                    
                    void some() {
                        var a = new A(1, 2, 3);
                        var x = a.x;
                        var y = a.y();
                    }
                }
                """,
            """
                class js$Test extends Object {
                  static A = class extends Object {
                    constructor(x, y, z) {
                      let __init__ = () => {
                        this.x = 0
                        this.y = 0
                        this.z = 0
                      };
                      super();
                      __init__();
                      this.x = x;
                      this.y = y;
                      this.z = z;
                    }
                  }
                                
                  constructor() {
                    super();
                  }
                                
                  some() {
                    let a = new js$Test.A(1, 2, 3);
                    let x = a.x;
                    let y = a.y;
                  }
                }""");
    }
}