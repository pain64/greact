package lowering;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;
import java.util.function.Supplier;

import static util.CompileAssert.assertCompiled;
import static util.CompileAssert.assertCompiledMany;

public class _03CallAndRefLocalTest {

    @Test void call() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void bar(int x, int y) {}  
                  void baz() {
                    bar(42, 42);
                    this.bar(42, 42); 
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _bar(x, y) {
                  }
                  _baz() {
                    this._bar(42, 42);
                    this._bar(42, 42);
                  }
                }
                """);
    }

    @Test void callStatic() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  static void bar(int x, int y) {}
                  void baz() { bar(42, 42); }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  static _bar(x, y) {
                  }
                  _baz() {
                    this.constructor._bar(42, 42);
                  }
                }
                """);
    }

    @Test void callFromObject() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void baz() {
                    ((Object) 42).toString();
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _baz() {
                    (42).toString();
                  }
                }
                """);
    }

    @Test
    void callStaticGenericExternal() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    public class A<T> {
                      static int bar() { return 42; }
                      static int baz() { return 42; }
                      
                    }""",
                """
                    class js_A {
                      constructor() {
                      }
                      static _bar() {
                        return 42;
                      }
                      static _baz() {
                        return 42;
                      }
                    }
                    """),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    import static js.A.baz;
                    public class B {
                      void fizz() {
                        A.bar();
                        baz();
                      }
                    }""",
                """
                    class js_B {
                      constructor() {
                      }
                      _fizz() {
                        js_A._bar();
                        js_A._baz();
                      }
                    }
                    """)
        );
    }

    @Test void callOverloaded() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  void bar(int x) {}
                  void bar(long x) {}
                  
                  void baz() {
                    bar(42);
                    bar(42L);
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _bar($over, ...__args) {
                    if($over === 0) {
                      const [x] = __args;
                    } else if($over === 1) {
                      const [x] = __args;
                    }
                  }
                  _baz() {
                    this._bar(0, 42);
                    this._bar(1, 42);
                  }
                }
                """);
    }

    @Test void callVararg() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  int bar(int acc, int ...args) {
                    for(var arg: args) acc += arg;
                    return acc;
                  }
                  
                  void baz() {
                    bar(42, 1, 2, 3);
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _bar(acc, ...args) {
                    for(let arg of args)
                      acc += arg;
                    return acc;
                  }
                  _baz() {
                    this._bar(42, 1, 2, 3);
                  }
                }
                """);
    }

    @Test void varargOverloaded() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  int bar(int acc, int ...args) {
                    return 0;
                  }
                  int bar(float acc, float init, int ...args) {
                    return 0;
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _bar($over, ...__args) {
                    if($over === 0) {
                      const [acc, ...args] = __args;
                      return 0;
                    } else if($over === 1) {
                      const [acc, init, ...args] = __args;
                      return 0;
                    }
                  }
                }
                """);
    }

    @Test void methodRefLocal() throws IOException {
        assertCompiled(
            """
                package js;
                import com.greact.model.ErasedInterface;
                import java.util.function.Function;
                import java.util.function.Consumer;
                public class Test {
                  @ErasedInterface
                  @FunctionalInterface
                  interface HInt {
                    void handle(int x);
                  }
                  @ErasedInterface
                  @FunctionalInterface
                  interface HLong {
                    void handle(long x);
                  }
                  @Deprecated
                  static void foo(int x) {}
                  void fiz () {}
                  void bar(int x) {}
                  void bar(long x) {}
                  
                  static class A {
                    A(int x) {}
                    A(int y, int z) {}
                    void bar(int x) {};
                  };
                  
                  void baz() {
                    HInt m1 = Test::foo;
                    HLong m2 = this::bar;
                    HInt m3 = this::bar;
                    Function<Integer, A> s = A::new;
                    var a = new A(1);
                    HInt m4 = a::bar;
                    Consumer<Test> m5 = Test::fiz;
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  static _foo(x) {
                  }
                  _fiz() {
                  }
                  _bar($over, ...__args) {
                    if($over === 0) {
                      const [x] = __args;
                    } else if($over === 1) {
                      const [x] = __args;
                    }
                  }
                  static A = class {
                    constructor($over, ...__args) {
                      if($over === 1) {
                        const [x] = __args;
                      } else if($over === 2) {
                        const [y, z] = __args;
                      }
                    }
                    _bar(x) {
                    }
                  }
                  _baz() {
                    const m1 = js_Test._foo.bind(js_Test);
                    const m2 = this._bar.bind(this, 1);
                    const m3 = this._bar.bind(this, 0);
                    const s = ((x) => new js_Test.A(1, x));
                    const a = new js_Test.A(1, 1);
                    const m4 = a._bar.bind(a);
                    const m5 = ((self) => self._fiz());
                  }
                }
                """);
    }

    @Test
    void callFunctionalInterface() throws IOException {
        assertCompiled(
            """
                package js;
                import com.greact.model.ErasedInterface;
                public class Test {
                  @ErasedInterface
                  @FunctionalInterface
                  interface HInt {
                    void handle(int x);
                  }
                  
                  void baz() {
                    HInt m1 = (x) -> {};
                    m1.handle(42);
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _baz() {
                    const m1 = (x) => {
                    };
                    m1(42);
                  }
                }
                """);
    }

    @Test
    void callOverloadedMany() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    public class A {
                      void mA(int x) {}
                      void mA(long x) {}
                      void callB(B b) {
                        b.mB(1L);
                        b.mB(1);
                      }
                    }""",
                """
                    class js_A {
                      constructor() {
                      }
                      _mA($over, ...__args) {
                        if($over === 0) {
                          const [x] = __args;
                        } else if($over === 1) {
                          const [x] = __args;
                        }
                      }
                      _callB(b) {
                        b._mB(1, 1);
                        b._mB(0, 1);
                      }
                    }
                    """),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    public class B {
                      void mB(int x) {}
                      void mB(long x) {}
                      void callA(A a) {
                        a.mA(1L);
                        a.mA(1);
                      }
                    }""",
                """
                    class js_B {
                      constructor() {
                      }
                      _mB($over, ...__args) {
                        if($over === 0) {
                          const [x] = __args;
                        } else if($over === 1) {
                          const [x] = __args;
                        }
                      }
                      _callA(a) {
                        a._mA(1, 1);
                        a._mA(0, 1);
                      }
                    }
                    """)
        );
    }
}