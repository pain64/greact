package lowering;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;

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
}
