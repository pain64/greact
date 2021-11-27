package lowering;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.CompileAssert.*;

public class _00DeclarationsTest {

    @Test
    void klass() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {}""",
            """
                class js$Test extends Object {
                  constructor() {
                    super();
                  }              
                }""");
    }

    @Test
    void klassExtendsGenericType() throws IOException {
        assertCompiledMany(
            new CompileCase("js.A",
                """
                    package js;
                    public class A<T> {
                    }""",
                """
                    class js$A extends Object {
                      constructor() {
                        super();
                      }
                    }"""),
            new CompileCase("js.B",
                """
                    package js;
                    public class B extends A<String> {
                    }""",
                """
                    class js$B extends js$A {
                      constructor() {
                        super();
                      }
                    }""")
        );
    }

    @Test
    void method() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {
                  static void bar() {}
                  void foo(int x, String y) {}
                  private void foobar() {}
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                    
                  static bar() {
                  }
                  
                  foo(x, y) {
                  }
                  
                  foobar() {
                  }
                }""");
    }

    @Test
    void abstractMethod() throws IOException {
        assertCompiled(
            """
                package js;
                public abstract class Test {
                  abstract void bar();
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                                
                                
                }""");
    }

    @Test
    void overloadedMethod() throws IOException {
        assertCompiled(
            """
               package js;
               public class Test {
                 void baz() {}
                 void foo() {}
                 void bar() {}
                 void bar(int x) {}
                 void bar(long x) {}
               }""",
            """
                    class js$Test extends Object {
                      constructor() {
                        super();
                      }
                                    
                      baz() {
                      }
                                    
                      foo() {
                      }
                                    
                      bar($over, ...__args) {
                        switch($over) {
                          case 0:
                            break
                          case 1:
                            var [x] = __args;
                            break
                          case 2:
                            var [x] = __args;
                            break
                        }
                      }
                    }""");

    }

    @Test
    void localVar() throws IOException {
        assertCompiled(
            """
                package js;
                public class Test {                  
                  void baz() {
                    String x;
                  }
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  
                  baz() {
                    let x = null;
                  }
                }""");
    }

//    @Test
//    void innerClasses() throws IOException {
//        // FIXME: expected 1 java input and 2 js output files
//        // TODO: move this to ordering test
//        assertCompiledMany(
//            new CompileCase(
//                "js.Test",
//                """
//                    package js;
//                    public class Test {
//                      void bar() {};
//                    }""",
//                """
//                    class js$Test {
//                      bar() {
//                      }
//                    }"""),
//            new CompileCase(
//                "js.Test2",
//                """
//                    package js;
//                    public class Test2 {
//                      void baz() {};
//                    }""",
//                """
//                    class js$Test2 {
//                      baz() {
//                      }
//                    }""")
//        );
//    }
}