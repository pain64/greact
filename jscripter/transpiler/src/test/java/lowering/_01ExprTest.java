package lowering;

import org.junit.jupiter.api.Test;
import util.CompileAssert.CompileCase;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;
import static util.CompileAssert.assertCompiledMany;

public class _01ExprTest {
    @Test void literal() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void baz() {
                    boolean x0 = true;
                    boolean x1 = false;
                    int x2 = 42;
                    long x3 = 42;
                    float x4 = 42.0f;
                    double x5 = 42.0d;
                    Character x6 = 'A';
                    String x7 = "hello";
                    Object x8 = null;
                    String x9 = \"\"\"
                      'escaped single quote'
                    \"\"\";
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _baz() {
                    const x0 = true;
                    const x1 = false;
                    const x2 = 42;
                    const x3 = 42;
                    const x4 = 42.0;
                    const x5 = 42.0;
                    const x6 = 'A';
                    const x7 = 'hello';
                    const x8 = null;
                    const x9 = '  \\'escaped single quote\\'\\n';
                  }
                }
                """);
    }

    @Test
    void assignment() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  int z;
                  void baz() {
                    int x;
                    x = 42;
                    z = 42;
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                    const __init__ = () => {
                      this.z = 0;
                    };
                    __init__();
                  }
                  _baz() {
                    const x = null;
                    x = 42;
                    this.z = 42;
                  }
                }
                """);
    }

    @Test
    void varId() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void baz() {
                    boolean x = true;
                    boolean y = x;
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _baz() {
                    const x = true;
                    const y = x;
                  }
                }
                """);
    }

    @Test
    void ternary() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void boolToInt() {
                    int x = true ? 1 : 0;
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _boolToInt() {
                    const x = true ? 1 : 0;
                  }
                }
                """);
    }

    @Test
    void unaryOp() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void baz() {
                    int x = 0;
                    int x1 = x++;
                    int x2 = x--;
                    int x3 = ++x;
                    int x4 = --x;
                    int x5 = +1;
                    int x6 = -1;
                    int x7 = ~1;
                    boolean y = true;
                    boolean y1 = !y;
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _baz() {
                    let x = 0;
                    const x1 = x++;
                    const x2 = x--;
                    const x3 = ++x;
                    const x4 = --x;
                    const x5 = +1;
                    const x6 = -1;
                    const x7 = ~1;
                    const y = true;
                    const y1 = !y;
                  }
                }
                """);
    }

    @Test
    void binaryOp() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void baz() {
                    int x0 = 1 * 1;
                    int x1 = 1 / 2;
                    float d1 = 1.0f / 2.0f;
                    var d2 = 1 / 2;
                    var d3 = 1.0f / 2.0f;
                    int x2 = 1 % 1;
                    int x3 = 1 + 1;
                    int x4 = 1 - 1;
                    int y0 = 1 << 1;
                    int y1 = 1 >> 1;
                    int y2 = 1 >>> 1;
                    boolean z0 = 1 < 1;
                    boolean z1 = 1 > 1;
                    boolean z2 = 1 <= 1;
                    boolean z3 = 1 >= 1;
                    boolean z4 = 1 == 1;
                    boolean z5 = 1 != 1;
                    int k0 = 1 & 1;
                    int k1 = 1 ^ 1;
                    int k2 = 1 | 1;
                    boolean b0 = true && false;
                    boolean b1 = true || false;
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _baz() {
                    const x0 = 1 * 1;
                    const x1 = Math.floor(1 / 2);
                    const d1 = 1.0 / 2.0;
                    const d2 = Math.floor(1 / 2);
                    const d3 = 1.0 / 2.0;
                    const x2 = 1 % 1;
                    const x3 = 1 + 1;
                    const x4 = 1 - 1;
                    const y0 = 1 << 1;
                    const y1 = 1 >> 1;
                    const y2 = 1 >>> 1;
                    const z0 = 1 < 1;
                    const z1 = 1 > 1;
                    const z2 = 1 <= 1;
                    const z3 = 1 >= 1;
                    const z4 = 1 === 1;
                    const z5 = 1 !== 1;
                    const k0 = 1 & 1;
                    const k1 = 1 ^ 1;
                    const k2 = 1 | 1;
                    const b0 = true && false;
                    const b1 = true || false;
                  }
                }
                """);
    }

    @Test
    void compoundAssignment() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  int z;
                  void baz() {
                    int x = 0;
                    x *= 1;
                    x /= 1;
                    x %= 1;
                    x += 1;
                    x -= 1;
                    x <<= 1;
                    x >>= 1;
                    x >>>= 1;
                    x &= 1;
                    x ^= 1;
                    x |= 1;
                    z += 1;
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                    const __init__ = () => {
                      this.z = 0;
                    };
                    __init__();
                  }
                  _baz() {
                    let x = 0;
                    x *= 1;
                    x /= 1;
                    x %= 1;
                    x += 1;
                    x -= 1;
                    x <<= 1;
                    x >>= 1;
                    x >>>= 1;
                    x &= 1;
                    x ^= 1;
                    x |= 1;
                    this.z += 1;
                  }
                }
                """);
    }

    @Test
    void newArray() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void baz() {
                    var x = new int[42];
                    var y = new String[] {"hello", "world"};
                    var x1 = new int[42][42];
                    var y2 = new String[][] {
                      new String[]{"1", "2"},
                      new String[]{"3", "4"}
                    };
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _baz() {
                    const x = [];
                    const y = ['hello', 'world'];
                    const x1 = [];
                    const y2 = [['1', '2'], ['3', '4']];
                  }
                }
                """);
    }

    @Test
    void arrayAccess() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void baz(int[][] x) {
                    int[] y0 = x[0];
                    int y1 = x[0][0];
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _baz(x) {
                    const y0 = x[0];
                    const y1 = x[0][0];
                  }
                }
                """);
    }

    @Test
    void memberSelect() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  static class A {
                    int field;
                    A next;
                  }
                  void baz(A a) {
                    int x = a.field;
                    A b = a.next;
                    int y = a.next.field;
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  static A = class {
                    constructor() {
                      const __init__ = () => {
                        this.field = 0;
                        this.next = null;
                      };
                      __init__();
                    }
                  }
                  _baz(a) {
                    const x = a.field;
                    const b = a.next;
                    const y = a.next.field;
                  }
                }
                """);
    }

    @Test
    void typeCast() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void baz(int x) {
                    var y = (long) x;
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _baz(x) {
                    const y = x;
                  }
                }
                """);
    }

    @Test
    void parens() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void baz(int x) {
                    var y = ((x));
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _baz(x) {
                    const y = ((x));
                  }
                }
                """);
    }

    @Test
    void lambda() throws IOException {
        assertCompiled(
            """
                package js;
                import jstack.jscripter.transpiler.model.ErasedInterface;
                public class Test {
                  @ErasedInterface
                  @FunctionalInterface
                  interface My {
                    int apply(int i);
                  }
                  void baz() {
                    My lambda = (i) -> {
                      return i;
                    };
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _baz() {
                    const lambda = (i) => {
                      return i;
                    };
                  }
                }
                """);
    }

    @Test
    void lambdaWithParentMethodCall() throws IOException {
        assertCompiled(
            """
                package js;
                import jstack.jscripter.transpiler.model.ErasedInterface;
                public class Test {
                  @ErasedInterface
                  @FunctionalInterface
                  interface My {
                    void apply();
                  }
                  void foo() { }
                  
                  int xx = 1;
                  
                  void baz() {
                    My lambda = () -> {
                      int yy = xx;
                      foo();
                    };
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                    const __init__ = () => {
                      this.xx = 1;
                    };
                    __init__();
                  }
                  _foo() {
                  }
                  _baz() {
                    const lambda = () => {
                      const yy = this.xx;
                      this._foo();
                    };
                  }
                }
                """);
    }

    @Test
    void switchExpr() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  int baz(int x) {
                    return switch(x) {
                      case 1, 2 -> 41;
                      case 3 -> 42;
                      default -> {
                        var y = 52;
                        yield y;
                      }
                    };
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _baz(x) {
                    return (() => {
                      switch(x) {
                        case 1:
                        case 2:
                          return 41;
                        case 3:
                          return 42;
                        default:
                          {
                            const y = 52;
                            return y;
                          }
                      }
                    })();
                  }
                }
                """);
    }

    @Test
    void newClassExpr() throws IOException {
        // очень плохой тест:
        assertCompiled(
            """
                package js;
                public class Test {
                  static class A {
                    final int x;
                    final int y;
                    public A(int x, int y) {
                      this.x = x;
                      this.y = y;
                    }
                  }
                  A newA() {
                    return new A(1, 2);
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  static A = class {
                    constructor(x, y) {
                      const __init__ = () => {
                        this.x = 0;
                        this.y = 0;
                      };
                      __init__();
                      this.x = x;
                      this.y = y;
                    }
                  }
                  _newA() {
                    return new js_Test.A(1, 2);
                  }
                }
                """);
    }

    @Test
    void newClassParametrized() throws IOException {
        assertCompiledMany(
            new CompileCase("js.A",
                """
                    package js;
                    public class A<T> {}""",
                """
                    class js_A {
                      constructor() {
                      }
                    }
                    """),
            new CompileCase("js.Test",
                """
                    package js;
                    public class Test {
                      A a = new A<String>();
                    }""",
                """
                    class js_Test {
                      constructor() {
                        const __init__ = () => {
                          this.a = new js_A();
                        };
                        __init__();
                      }
                    }
                    """));
    }

    @Test
    void instanceOfForJSNativeAPI() throws IOException {
        assertCompiledMany(
            new CompileCase("js.A",
                """
                    package js;
                    import jstack.jscripter.transpiler.model.JSNativeAPI;
                    
                    @JSNativeAPI
                    class A {}""",
                """
                    """),
            new CompileCase("js.Test", """
                package js;
                public class Test {
                  void baz(Object x) {
                    boolean y1 = x instanceof A;
                  }
                }""",
                """
                    class js_Test {
                      constructor() {
                      }
                      _baz(x) {
                        const y1 = x instanceof A;
                      }
                    }
                    """));
    }

    @Test
    void instanceOfExpr() throws IOException {
        assertCompiledMany(
            new CompileCase("js.A",
                """
                    package js;
                    class A {}""",
                """
                    class js_A {
                      constructor() {
                      }
                    }
                    """),
            new CompileCase("js.Test", """
                package js;
                public class Test {
                  static class B {}

                  void baz(Object x) {
                    boolean y1 = x instanceof String;
                    boolean y2 = x instanceof Integer;
                    boolean y3 = x instanceof Long;
                    boolean y4 = x instanceof String s;
                    boolean y5 = x instanceof A;
                    boolean y6 = x instanceof B;
                  }
                }""",
                """
                    class js_Test {
                      constructor() {
                      }
                      static B = class {
                        constructor() {
                        }
                      }
                      _baz(x) {
                        let s;
                        const y1 = (($x) => {return typeof $x === 'string' || $x instanceof String})(x);
                        const y2 = typeof x == 'number';
                        const y3 = typeof x == 'number';
                        const y4 = (s = x)(($x) => {return typeof $x === 'string' || $x instanceof String})();
                        const y5 = x instanceof js_A;
                        const y6 = x instanceof js_Test.B;
                      }
                    }
                    """));
        // FIXME(generated for instanceof pattern) => const s;
    }

    @Test
    void instanceOfWithVariable() throws IOException {
        assertCompiledMany(
            new CompileCase("js.A",
                """
                    package js;
                                        
                    class A {
                        static class B {}
                        public void xxx(Object some) {
                            if (some instanceof A a) {
                            }
                            if(some instanceof A a) {
                            }
                            if(some instanceof B b) {
                            }
                        }
                    }""",
                """
                    class js_A {
                      constructor() {
                      }
                      static B = class {
                        constructor() {
                        }
                      }
                      _xxx(some) {
                        let a, b;
                        if((a = some) instanceof js_A) {
                        }if((a = some) instanceof js_A) {
                        }if((b = some) instanceof js_A.B) {
                        }}
                    }
                    """));
    }

    @Test
    void equals() throws IOException {
        assertCompiledMany(
            new CompileCase("js.A",
                """
                    package js;
                                        
                    class A {
                        public void xxx(Object a, Object b) {
                            var flag = a.equals(b);
                            flag = !a.equals(b);
                        }
                    }""",
                """
                    class js_A {
                      constructor() {
                      }
                      _xxx(a, b) {
                        let flag = a == (b);
                        flag = a !== (b);
                      }
                    }
                    """));
    }
}