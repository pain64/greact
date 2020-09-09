package lowering;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;

public class _03CallTest {

    @Test
    void callLocal() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  static class B {
                    void foo() {}
                  }
                  static class A {
                    B b;
                  }
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
    void callLocalStatic() throws IOException {
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
}
