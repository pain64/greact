package lowering;

import com.greact.generate.util.CompileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;
import static util.CompileAssert.assertCompiledMany;

public class _08AsyncAwait {
    @Test void asyncOverloadedSibling() throws IOException {
        assertCompiled(
            """
                package js;
                import com.greact.model.async;
                public class Test {
                  @async void foo() {}
                  @async void foo(int x) {}
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  
                  async foo($over, ...__args) {
                    switch($over) {
                      case 0:
                        break
                      case 1:
                        var [x] = __args;
                        break
                    }
                  }
                }""");
    }

    @Test void asyncOverloadedSiblingFail() throws IOException {
        try {
            assertCompiled(
                """
                    package js;
                    import com.greact.model.async;
                    public class Test {
                      @async void foo() {}
                      void foo(int x) {}
                    }""",
                """
                    """);
        } catch (Exception ex) {
            var ce = (CompileException) ex.getCause();
            Assertions.assertSame(ce.error, CompileException.ERROR.MUST_BE_DECLARED_AS_ASYNC);
        }
    }

    @Test void callAsyncMethod() throws IOException {
        assertCompiled(
            """
                package js;
                import com.greact.model.async;
                public class Test {
                  @async void foo() {}
                  @async void bar() { foo(); }
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  
                  async foo() {
                  }
                  
                  async bar() {
                    (await this.foo());
                  }
                }""");
    }

    @Test void callAsyncMethodInNotAsyncFail() throws IOException {
        try {
            assertCompiled(
                """
                    package js;
                    import com.greact.model.async;
                    public class Test {
                      @async void foo() {}
                      void bar() { foo(); }
                    }""",
                """
                    """);
        } catch (Exception ex) {
            var ce = (CompileException) ex.getCause();
            Assertions.assertSame(ce.error, CompileException.ERROR.MUST_BE_DECLARED_AS_ASYNC);
        }
    }

    @Test void callAsyncMethodInInterface() throws IOException {
        assertCompiled(
            """
                package js;
                import com.greact.model.async;
                public class Test {
                  interface Foo {
                    @async void foo();
                  }
                  @async void callee(Foo f) { f.foo(); }
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                  
                  async callee(f) {
                    (await f.foo());
                  }
                }""");
    }


    @Test void asyncInSuperType() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    import com.greact.model.async;
                    public class A {
                      @async void foo() {}
                    }""",
                """
                    class js$A extends Object {
                      constructor() {
                        super();
                      }
                      
                      async foo() {
                      }
                    }"""),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    class B extends A{
                      @Override void foo() {
                      }
                    }""",
                """
                    class js$B extends js$A {
                      constructor() {
                        super();
                      }
                      
                      async foo() {
                      }
                    }"""));
    }

    @Test void asyncInInterface() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    import com.greact.model.async;
                    public interface A {
                      @async void foo();
                    }""",
                """
                    """),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    class B implements A {
                      @Override public void foo() {
                      }
                    }""",
                """
                    class js$B extends Object {
                      constructor() {
                        super();
                      }
                      
                      async foo() {
                      }
                    }"""));
    }

    @Test void notAsyncInSuperButTriedLocalFail() throws IOException {
        try {
            assertCompiledMany(
                new CompileAssert.CompileCase("js.A",
                    """
                        package js;
                        public interface A {
                          void foo();
                        }""",
                    """
                        """),
                new CompileAssert.CompileCase("js.B",
                    """
                        package js;
                        import com.greact.model.async;
                        class B implements A {
                          @async @Override public void foo() {
                          }
                        }""",
                    """
                        """));
        } catch (Exception ex) {
            var ce = (CompileException) ex.getCause();
            Assertions.assertSame(ce.error, CompileException.ERROR.CANNOT_BE_DECLARED_AS_ASYNC);
        }
    }

    @Test void asyncLambda() throws IOException {
        assertCompiled(
            """
                package js;
                import com.greact.model.async;
                public class Test {
                  interface Foo {
                    @async void foo();
                  }
                  @async void doo() {};
                  void bar() {
                    Foo instance = () -> { doo(); }; 
                  }
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                                
                  async doo() {
                  }
                                
                  bar() {
                    let instance = async () => {
                      (await this.doo());
                    };
                  }
                }""");
    }

    @Test void callAsyncLambda() throws IOException {
        assertCompiled(
            """
                package js;
                import com.greact.model.async;
                public class Test {
                  interface Foo {
                    @async void foo();
                  }
                  Foo instance = () -> {};
                }""",
            """
                class js$Test extends Object {
                  constructor() {
                    let __init__ = () => {
                      this.instance = async () => {
                      }
                    };
                    super();
                    __init__();
                  }
                }""");
    }
}
