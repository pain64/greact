package lowering;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

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
                class js$Test {
                  bar(x, y) {
                  }
                  
                  baz() {
                    this.bar(42, 42)
                    this.bar(42, 42)
                  }
                }""");
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
                class js$Test {
                  static bar(x, y) {
                  }
                  
                  baz() {
                    this.constructor.bar(42, 42)
                  }
                }""");
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
                class js$Test {
                  bar$0(x) {
                  }
                  
                  bar$1(x) {
                  }
                  
                  baz() {
                    this.bar$0(42)
                    this.bar$1(42)
                  }
                }""");
    }

    @Test
    void methodRefLocal() throws IOException {
        assertCompiled(
            """
                package js;
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
                  void bar(int x) {}
                  void bar(long x) {}
                  
                  void baz() {
                    HInt m1 = Test::foo;
                    HLong m2 = this::bar;
                    HInt m3 = this::bar;
                  }
                }""",
            """
                class js$Test {
                  static foo(x) {
                  }
                  
                  bar$0(x) {
                  }
                  
                  bar$1(x) {
                  }
                  
                  baz() {
                    let m1 = js$Test.foo
                    let m2 = this.bar$1.bind(this)
                    let m3 = this.bar$0.bind(this)
                  }
                }""");
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
                    class js$A {
                      mA$0(x) {
                      }
                      
                      mA$1(x) {
                      }
                      
                      callB(b) {
                        b.mB$1(1)
                        b.mB$0(1)
                      }
                    }"""),
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
                    class js$B {
                      mB$0(x) {
                      }
                      
                      mB$1(x) {
                      }
                      
                      callA(a) {
                        a.mA$1(1)
                        a.mA$0(1)
                      }
                    }""")
        );
    }
}