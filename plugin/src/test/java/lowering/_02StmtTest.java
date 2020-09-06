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
    void stmtLabel() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void baz(int x, int y) {
                    outer:
                    for (;;)
                      for (;;)
                        break outer;
                  }
                }""",
            """
                class js$Test {
                  baz(x, y) {
                    x++
                    l1:
                    y++
                    while(true) {
                      break l1
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

    @Test
    void stmtFor() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void baz() {
                    for(int x = 0, y = 0; true; x++, y++) {} 
                  }
                }""",
            """
                class js$Test {
                  baz() {
                    for(let x = 0, y = 0; true; x++, y++) {
                    }
                  }
                }""");
    }

    @Test
    void stmtForEach() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void baz(int[] array) {
                    for(var x : array) {}
                  }
                }""",
            """
                class js$Test {
                  baz(array) {
                    for(let x in array) {
                    }
                  }
                }""");
    }
}
