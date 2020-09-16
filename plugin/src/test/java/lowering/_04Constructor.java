package lowering;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;

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
                class js$Test {
                  constructor() {
                    this.x = null
                    this.y = null
                    this.z = 42
                    this.f = 0
                  }
                  static new$0(x, y) {
                    let self = new js$Test()
                    self.x = x
                    self.y = y
                    return self
                  }
                  
                  static new$1(x) {
                    let self = new js$Test()
                    self.x = x
                    self.y = 'hello'
                    return self
                  }
                }""");
    }
}
