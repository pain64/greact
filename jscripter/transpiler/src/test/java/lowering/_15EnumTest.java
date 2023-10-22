package lowering;

import org.junit.jupiter.api.Test;
import util.CompileAssert;
import util.CompileAssert.CompileCase;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;
import static util.CompileAssert.assertCompiledMany;

public class _15EnumTest {
    // TODO: сделать поддержку методов (с учетом перегрузки)
    @Test void enumTestSimple() throws IOException {
        assertCompiledMany(
            new CompileCase("js.Test", """
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
                      static __Inst = class extends Object {
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
                      static FOO = new this.__Inst(1);
                      static BAR = new this.__Inst(2);
                      static BAZ = new this.__Inst(3);

                      static __match(self) {
                        switch(self) {
                          case 'FOO': return this.FOO;
                          case 'BAR': return this.BAR;
                          case 'BAZ': return this.BAZ;
                        }
                      }
                      static x(self) {
                        return __match(self).x;
                      }
                    }
                    """),
            new CompileCase("js.Bar", """
                package js;
                import static js.Test.FOO;
                class Bar {
                  boolean bar() {
                    var t = Test.FOO;
                    return t == FOO || t == Test.BAR;
                  }
                }
                """,
                """
                    class js_Bar {
                      constructor() {
                      }
                      _bar() {
                        const t = 'FOO';
                        return t === 'FOO' || t === 'BAR';
                      }
                    }
                    """),
            new CompileCase("js.Baz", """
                package js;
                import static js.Test.FOO;
                class Baz {
                  record X(Test t) {}
                  boolean baz() {
                    var x = new X(Test.FOO);
                    return x.t == FOO || x.t == Test.BAR;
                  }
                }
                """,
                """
                    class js_Baz {
                      constructor() {
                      }
                      static X = class {
                        constructor(t) {
                          this.t = t;
                        }
                      }
                      _baz() {
                        const x = new js_Baz.X('FOO');
                        return x.t === 'FOO' || x.t === 'BAR';
                      }
                    }
                    """)
        );
    }
}
