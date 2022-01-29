package lowering;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;
import java.util.function.Supplier;

import static util.CompileAssert.assertCompiled;
import static util.CompileAssert.assertCompiledMany;

public class _03CallAndRefLocalTest {

    @Test
    void call() throws IOException {
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
                    class js$Test extends Object {
                      constructor() {
                        super();
                      }
                      bar(x, y) {
                      }
                      baz() {
                        this.bar(42, 42);
                        this.bar(42, 42);
                      }
                    }
                    """);
    }

    @Test
    void callStatic() throws IOException {
        assertCompiled(
                """
                    package js;
                    public class Test {
                      static void bar(int x, int y) {} 
                      void baz() { bar(42, 42); }
                    }""",
                """
                    class js$Test extends Object {
                      constructor() {
                        super();
                      }
                      static bar(x, y) {
                      }
                      baz() {
                        this.constructor.bar(42, 42);
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
                            class js$A extends Object {
                              constructor() {
                                super();
                              }
                              static bar() {
                                return 42;
                              }
                              static baz() {
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
                            class js$B extends Object {
                              constructor() {
                                super();
                              }
                              fizz() {
                                js$A.bar();
                                js$A.baz();
                              }
                            }
                            """)
        );
    }

    @Test
    void callOverloaded() throws IOException {
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
                        class js$Test extends Object {
                          constructor() {
                            super();
                          }
                          bar($over, ...__args) {
                            if($over === 0) {
                              const [x] = __args;
                            } else if($over === 1) {
                              const [x] = __args;
                            }
                          }
                          baz() {
                            this.bar(0, 42);
                            this.bar(1, 42);
                          }
                        }
                        """);
    }

    @Test void methodRefLocal() throws IOException {
        assertCompiled(
                """
                    package js;
                    import java.util.function.Function;
                    import java.util.function.Consumer;
                    public class Test {
                      @FunctionalInterface
                      interface HInt {
                        void handle(int x);
                      }
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
                    class js$Test extends Object {
                      constructor() {
                        super();
                      }
                      static foo(x) {
                      }
                      fiz() {
                      }
                      bar($over, ...__args) {
                        if($over === 0) {
                          const [x] = __args;
                        } else if($over === 1) {
                          const [x] = __args;
                        }
                      }
                      static A = class extends Object {
                        constructor($over, ...__args) {
                          if($over === 1) {
                            const [x] = __args;
                            super();
                          } else if($over === 2) {
                            const [y, z] = __args;
                            super();
                          }
                        }
                        bar(x) {
                        }
                      }
                      baz() {
                        const m1 = js$Test.foo.bind(js$Test);
                        const m2 = this.bar.bind(this, 1);
                        const m3 = this.bar.bind(this, 0);
                        const s = ((x) => new js$Test.A(1, x));
                        const a = new js$Test.A(1, 1);
                        const m4 = a.bar.bind(a);
                        const m5 = ((self) => self.fiz());
                      }
                    }
                    """);
    }

    @Test
    void callFunctionalInterface() throws IOException {
        assertCompiled(
                """
                    package js;
                    public class Test {
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
                    class js$Test extends Object {
                      constructor() {
                        super();
                      }
                      baz() {
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
                                class js$A extends Object {
                                  constructor() {
                                    super();
                                  }
                                  mA($over, ...__args) {
                                    if($over === 0) {
                                      const [x] = __args;
                                    } else if($over === 1) {
                                      const [x] = __args;
                                    }
                                  }
                                  callB(b) {
                                    b.mB(1, 1);
                                    b.mB(0, 1);
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
                                class js$B extends Object {
                                  constructor() {
                                    super();
                                  }
                                  mB($over, ...__args) {
                                    if($over === 0) {
                                      const [x] = __args;
                                    } else if($over === 1) {
                                      const [x] = __args;
                                    }
                                  }
                                  callA(a) {
                                    a.mA(1, 1);
                                    a.mA(0, 1);
                                  }
                                }
                                """)
        );
    }
}