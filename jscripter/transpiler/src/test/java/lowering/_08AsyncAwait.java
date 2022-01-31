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
                public abstract class Test {
                  @async abstract void bar();
                  void foo() {}
                  @async void foo(int x) { bar(); }
                }""",
            """
                class js_Test extends Object {
                  constructor() {
                    super();
                  }
                  async _foo($over, ...__args) {
                    if($over === 0) {
                    } else if($over === 1) {
                      const [x] = __args;
                      (await this._bar());
                    }
                  }
                }
                """);
    }

    @Test void asyncOverloadedSiblingFail() throws IOException {
        try {
            assertCompiled(
                """
                    package js;
                    import com.greact.model.async;
                    public abstract class Test {
                      @async abstract void bar();
                      @async void foo() {}
                      @async void foo(int x) { bar(); }
                    }""",
                """
                    """);
        } catch (Exception ex) {
            var ce = (CompileException) ex.getCause();
            Assertions.assertSame(CompileException.ERROR.CANNOT_BE_DECLARED_AS_ASYNC, ce.error);
        }
    }

    @Test void asyncConstructorFail() throws IOException {
        try {
            assertCompiled(
                """
                    package js;
                    import com.greact.model.async;
                    public class Test {
                      @async Test() {}
                    }""",
                """
                    """);
        } catch (Exception ex) {
            var ce = (CompileException) ex.getCause();
            Assertions.assertSame(CompileException.ERROR.CANNOT_BE_DECLARED_AS_ASYNC, ce.error);
        }
    }

    @Test void callAsyncMethod() throws IOException {
        assertCompiled(
            """
                package js;
                import com.greact.model.async;
                public abstract class Test {
                  @async abstract void foo();
                  @async void bar() { foo(); }
                }""",
            """
                class js_Test extends Object {
                  constructor() {
                    super();
                  }
                  async _bar() {
                    (await this._foo());
                  }
                }
                """);
    }

    @Test void callAsyncMethodInNotAsyncFail() throws IOException {
        try {
            assertCompiled(
                """
                    package js;
                    import com.greact.model.async;
                    public abstract class Test {
                      @async abstract void foo();
                      void bar() { foo(); }
                    }""",
                """
                    """);
        } catch (Exception ex) {
            var ce = (CompileException) ex.getCause();
            Assertions.assertSame(CompileException.ERROR.ASYNC_INVOCATION_NOT_ALLOWED, ce.error);
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
                class js_Test extends Object {
                  constructor() {
                    super();
                  }
                  async _callee(f) {
                    (await f._foo());
                  }
                }
                """);
    }


    @Test void asyncInSuperType() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    import com.greact.model.async;
                    public abstract class A {
                      @async abstract void foo();
                    }""",
                """
                    class js_A extends Object {
                      constructor() {
                        super();
                      }
                    }
                    """),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    class B extends A {
                      @Override void foo() {
                      }
                    }""",
                """
                    class js_B extends js_A {
                      constructor() {
                        super();
                      }
                      _foo() {
                      }
                    }
                    """));
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
                    class js_B extends Object {
                      constructor() {
                        super();
                      }
                      _foo() {
                      }
                    }
                    """));
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
                public abstract class Test {
                  interface Foo {
                    @async void foo();
                  }
                  @async abstract void doo();
                  void bar() {
                    Foo instance = () -> { doo(); };
                  }
                }""",
            """
                class js_Test extends Object {
                  constructor() {
                    super();
                  }
                  _bar() {
                    const instance = async () => {
                      (await this._doo());
                    };
                  }
                }
                """);
    }

    @Test void asyncClassInitFail() throws IOException {
        try {
            assertCompiled(
                """
                    package js;
                    import com.greact.model.async;
                    public class Test {
                      interface Foo {
                        @async void foo();
                      }
                      Foo instance = () -> {};
                      {
                        instance.foo();
                      }
                    }""",
                """
                    class js_Test extends Object {
                      constructor() {
                        const __init__ = () => {
                          this.instance = async () => {
                          };
                        };
                        super();
                        __init__();
                      }
                    }
                    """);
        } catch (Exception ex) {
            var ce = (CompileException) ex.getCause();
            Assertions.assertSame(ce.error, CompileException.ERROR.ASYNC_INVOCATION_NOT_ALLOWED);
        }
    }

    @Test void asyncLambdaExpressionOptimization() throws IOException {
        // FIXME: ((Foo) () -> { doo(); }).foo();
        assertCompiled(
            """
                package js;
                import com.greact.model.async;
                public class Test {
                  interface Foo {
                    @async void foo();
                  }
                  void doo() {};
                  void bar() {
                    Foo x;
                    (x = () -> { doo(); }).foo();
                  }
                }""",
            """
                class js_Test extends Object {
                  constructor() {
                    super();
                  }
                  _doo() {
                  }
                  _bar() {
                    const x = null;
                    (x = () => {
                      this._doo();
                    })._foo();
                  }
                }
                """);
    }
}
