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
                class js$Test extends Object {
                  constructor() {
                    super()
                  }
                  
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
                class js$Test extends Object {
                  constructor() {
                    super()
                  }
                  
                  baz() {
                    if(false)
                      return 41
                    if(true)
                      return 42
                    else
                      return 41
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
                class js$Test extends Object {
                  constructor() {
                    super()
                  }
                  
                  baz(x, y) {
                    while(true) {
                      x++
                      if(x > 10)
                        continue
                      if(x > 100)
                        break
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
                  void baz() {
                    outer:
                    for (;;)
                      for (;;)
                        continue outer;
                  }
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    super()
                  }
                  
                  baz() {
                    outer:
                    for(;;)
                      for(;;)
                        continue outer
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
                class js$Test extends Object {
                  constructor() {
                    super()
                  }
                  
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
                class js$Test extends Object {
                  constructor() {
                    super()
                  }
                  
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
                class js$Test extends Object {
                  constructor() {
                    super()
                  }
                  
                  baz(array) {
                    for(let x in array) {
                    }
                  }
                }""");
    }

    @Test
    void stmtSwitch() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void baz(int x, int y) {
                    switch(x) {
                      case 0:
                        y = 42;
                      case 1:
                      case 2:
                        y = 41;
                      default:
                        y = 40;
                    }
                  }
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    super()
                  }
                
                  baz(x, y) {
                    switch(x) {
                      case 0:
                        y = 42
                      case 1:
                      case 2:
                        y = 41
                      default:
                        y = 40
                    }
                  }
                }""");
    }

    @Test
    void stmtThrow() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void foo() throws Exception {
                    throw new Exception("xxx");
                  }
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    super()
                  }
                  
                  foo() {
                    throw new java$lang$Exception('xxx')
                  }
                }""");
    }

    @Test
    void stmtTry() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  static class E1 extends Exception {}
                  static class E2 extends Exception {}
                  static class E3 extends Exception {}
                  static class E4 extends Exception {}
                  
                  void foo() throws E1, E2, E3, E4 {
                  }
                  
                  void baz() throws E4 {
                    try {
                      foo();
                    } catch(E1 | E2 e) {
                      var x = e;
                    } catch(E3 ee) {
                      var y = ee;
                    } finally {
                      var z = 1;
                    }
                  }
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    super()
                  }
                  
                  foo() {
                  }
                  
                  baz() {
                    try {
                      this.foo()
                    } catch(e) {
                      if(e instanceof js$Test$E1 || e instanceof js$Test$E2) {
                        let x = e
                      } else if(e instanceof js$Test$E3) {
                        let ee = e
                        let y = ee
                      } else {
                        throw e
                      }
                    } finally {
                      let z = 1
                    }
                  }
                }""");
    }
}
