package lowering;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;

public class _15EnumTest {
    @Test void enumTestSimple() throws IOException {
        assertCompiled(
            """
                package js;
                  enum Test {
                    FOO(1), BAR(2), BAZ(3);
                    final int x;
                    Test(int x) {
                      this.x = x;
                    }
                    int twice() {
                      return x * 2;
                    }
                  }
                  
                """,
            """
                class js_Test extends Object {
                  static FOO = new js_Test(1);
                  static BAR = new js_Test(2);
                  static BAZ = new js_Test(3);
                  constructor(x) {
                    const __init__ = () => {
                      this.x = 0;
                    };
                    super();
                    __init__();
                    this.x = x;
                  }
                  _twice() {
                    return this.x * 2;
                  }
                }
                """);
    }

    //@Test
    void enumInnerClass() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  enum T {
                    FOO(1), BAR(2), BAZ(3);
                    final int x;
                    T(int x) {
                      this.x = x;
                    }
                    int twice() {
                      return x * 2;
                    }
                  }
                  void bar() {
                    T t = T.FOO;
                    var xx = t == T.BAR;
                    t.twice();
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  static T = class extends Object {
                    constructor(x) {
                      const __init__ = () => {
                        this.x = 0;
                      };
                      super();
                      __init__();
                      this.x = x;
                    }
                    _twice() {
                      return this.x * 2;
                    }
                  }
                  static {
                    js_Test.T.FOO = new js_Test.T(1);
                    js_Test.T.BAR = new js_Test.T(2);
                    js_Test.T.BAZ = new js_Test.T(3);
                  }
                  _bar() {
                    const t = js_Test.T.FOO;
                    const xx = t === js_Test.T.BAR;
                    t._twice();
                  }
                }
                """);
    }
}
