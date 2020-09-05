package lowering;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;

public class _02StmtTest {
    @Test
    void stmtReturn() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  int baz() { return 42; }
                }""",
            """
                class js$Test {
                  baz() {
                    return 42
                  }
                }""");
    }

    @Test
    void stmtIf() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  int baz() {
                    if(false) return 41;
                    
                    if(true) return 42;
                    else return 41; 
                  }
                }""",
            """
                class js$Test {
                  baz() {
                    if(false) {
                      return 41
                    }
                    if(true) {
                      return 42
                    } else {
                      return 41
                    }
                  }
                }""");
    }

    @Test
    void stmtWhile() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void baz(int x, int y) {
                    while(true) {
                      x++;
                      if(x > 10)
                        continue;
                      if(x > 100)
                        break;
                      y++;
                    } 
                  }
                }""",
            """
                class js$Test {
                  baz(x, y) {
                    while(true) {
                      x++
                      if(x > 10) {
                        continue
                      }
                      if(x > 100) {
                        break
                      }
                      y++
                    }
                  }
                }""");
    }

    @Test
    void stmtDoWhile() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void baz(int x) {
                    do {
                      x++;
                    } while(x < 100); 
                  }
                }""",
            """
                class js$Test {
                  baz(x) {
                    do {
                      x++
                    } while(x < 100)
                  }
                }""");
    }
}
