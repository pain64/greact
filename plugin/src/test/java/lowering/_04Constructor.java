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
                class js$Test extends Object {
                  constructor($over, x, y) {
                    switch($over) {
                      case 1:
                        super()
                        this.x = null
                        this.y = null
                        this.z = 42
                        this.f = 0
                        this.x = x
                        this.y = y
                        break
                      case 2:
                        super()
                        this.x = null
                        this.y = null
                        this.z = 42
                        this.f = 0
                        this.x = x
                        this.y = 'hello'
                        break
                    }
                  }
                }""");
    }
}
