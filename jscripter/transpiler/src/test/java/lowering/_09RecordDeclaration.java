package lowering;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;

public class _09RecordDeclaration {
    @Test void simple() throws IOException {
        assertCompiled(
                """
                    package js;
                    record Test(int x, int y, int z) {}
                    """,
                """
                    class js_Test extends Object {
                      constructor(x, y, z) {
                        super();
                        this.x = x;
                        this.y = y;
                        this.z = z;
                      }
                    }
                    """);
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
                class js_Test extends Object {
                  constructor() {
                    super();
                  }
                  static A = class extends Object {
                    constructor(x, y, z) {
                      super();
                      this.x = x;
                      this.y = y;
                      this.z = z;
                    }
                  }
                  _some() {
                    const a = new js_Test.A(1, 2, 3);
                    const x = a.x;
                    const y = a.y;
                  }
                }
                """);
    }
}