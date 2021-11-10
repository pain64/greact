package lowering;

import org.junit.jupiter.api.Test;
import util.CompileAssert;
import util.CompileAssert.CompileCase;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;
import static util.CompileAssert.assertCompiledMany;

public class _01ExprTest {

    @Test
    void literal() throws IOException {
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
                  }
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  
                  baz() {
                    let x0 = true;
                    let x1 = false;
                    let x2 = 42;
                    let x3 = 42;
                    let x4 = 42.0;
                    let x5 = 42.0;
                    let x6 = 'A';
                    let x7 = 'hello';
                    let x8 = null;
                  }
                }""");
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
                class js$Test extends Object {
                  constructor() {
                    let __init__ = () => {
                      this.z = 0
                    };
                    super();
                    __init__();
                  }
                  
                  baz() {
                    let x = null;
                    x = 42;
                    this.z = 42;
                  }
                }""");
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
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  
                  baz() {
                    let x = true;
                    let y = x;
                  }
                }""");
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
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  
                  boolToInt() {
                    let x = true ? 1 : 0;
                  }
                }""");
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
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  
                  baz() {
                    let x = 0;
                    let x1 = x++;
                    let x2 = x--;
                    let x3 = ++x;
                    let x4 = --x;
                    let x5 = +1;
                    let x6 = -1;
                    let x7 = ~1;
                    let y = true;
                    let y1 = !y;
                  }
                }""");
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
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  
                  baz() {
                    let x0 = 1 * 1;
                    let x1 = Math.floor(1 / 2);
                    let d1 = 1.0 / 2.0;
                    let d2 = Math.floor(1 / 2);
                    let d3 = 1.0 / 2.0;
                    let x2 = 1 % 1;
                    let x3 = 1 + 1;
                    let x4 = 1 - 1;
                    let y0 = 1 << 1;
                    let y1 = 1 >> 1;
                    let y2 = 1 >>> 1;
                    let z0 = 1 < 1;
                    let z1 = 1 > 1;
                    let z2 = 1 <= 1;
                    let z3 = 1 >= 1;
                    let z4 = 1 == 1;
                    let z5 = 1 != 1;
                    let k0 = 1 & 1;
                    let k1 = 1 ^ 1;
                    let k2 = 1 | 1;
                    let b0 = true && false;
                    let b1 = true || false;
                  }
                }""");
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
                class js$Test extends Object {
                  constructor() {
                    let __init__ = () => {
                      this.z = 0
                    };
                    super();
                    __init__();
                  }
                  
                  baz() {
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
                }""");
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
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  
                  baz() {
                    let x = [];
                    let y = ['hello', 'world'];
                    let x1 = [];
                    let y2 = [['1', '2'], ['3', '4']];
                  }
                }""");
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
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  
                  baz(x) {
                    let y0 = x[0];
                    let y1 = x[0][0];
                  }
                }""");
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
                class js$Test extends Object {
                  static A = class extends Object {
                    constructor() {
                      let __init__ = () => {
                        this.field = 0
                        this.next = null
                      };
                      super();
                      __init__();
                    }
                  }
                    
                  constructor() {
                    super();
                  }
                  
                  baz(a) {
                    let x = a.field;
                    let b = a.next;
                    let y = a.next.field;
                  }
                }""");
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
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  
                  baz(x) {
                    let y = x;
                  }
                }""");
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
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  
                  baz(x) {
                    let y = ((x));
                  }
                }""");
    }

    @Test
    void lambda() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
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
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  
                  baz() {
                    let lambda = (i) => {
                      return i
                    };
                  }
                }""");
    }

    @Test
    void lambdaWithParentMethodCall() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
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
                class js$Test extends Object {
                  constructor() {
                    let __init__ = () => {
                      this.xx = 1
                    };
                    super();
                    __init__();
                  }
                  
                  foo() {
                  }
                  
                  baz() {
                    let lambda = () => {
                      let yy = this.xx;
                      this.foo();
                    };
                  }
                }""");
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
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  
                  baz(x) {
                    return (() => {
                      switch(x) {
                        case 1:
                        case 2:
                          return 41
                        case 3:
                          return 42
                        default:
                          let y = 52;
                          return y
                      }
                    })()
                  }
                }""");
    }

    @Test
    void instanceOfExpr() throws IOException {
        assertCompiledMany(
            new CompileCase("js.A",
                """
                    package js;
                    class A {}""",
                """
                    class js$A extends Object {
                      constructor() {
                        super();
                      }
                    }"""),
            new CompileCase("js.Test", """
                package js;
                public class Test {
                  void baz(Object x) {
                    boolean y1 = x instanceof String;
                    boolean y2 = x instanceof Integer;
                    boolean y3 = x instanceof Long;
                    boolean y4 = x instanceof String s;
                    boolean y5 = x instanceof A;
                  }
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  
                  baz(x) {
                    let y1 = (($x) => {return typeof $x === 'string' || $x instanceof String})(x);
                    let y2 = typeof x == 'number';
                    let y3 = typeof x == 'number';
                    let y4 = (s = x, (($x) => {return typeof $x === 'string' || $x instanceof String})(s));
                    let y5 = x instanceof js$A;
                  }
                }"""));
        // FIXME(generated for instanceof pattern) => let s;
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
                class js$Test extends Object {
                  static A = class extends Object {
                    constructor(x, y) {
                      let __init__ = () => {
                        this.x = 0
                        this.y = 0
                      };
                      super();
                      __init__();
                      this.x = x;
                      this.y = y;
                    }
                  }
                                      
                  constructor() {
                    super();
                  }
                  
                  newA() {
                    return new js$Test.A(1, 2)
                  }
                }""");
    }

    @Test
    void newClassParametrized() throws IOException {
        assertCompiledMany(
            new CompileCase("js.A",
                """
                    package js;
                    public class A<T> {}""",
                """
                    class js$A extends Object {
                      constructor() {
                        super();
                      }
                    }"""),
            new CompileCase("js.Test",
                """
                    package js;
                    public class Test {
                      A a = new A<String>();
                    }""",
                """
                    class js$Test extends Object {
                      constructor() {
                        let __init__ = () => {
                          this.a = new js$A()
                        };
                        super();
                        __init__();
                      }
                    }"""));
    }
}
