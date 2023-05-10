package lowering;

import jstack.jscripter.transpiler.generate.util.CompileException;
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
                import jstack.jscripter.transpiler.model.Async;
                public abstract class Test {
                  @Async abstract void bar();
                  void foo() {}
                  @Async void foo(int x) { bar(); }
                }""",
            """
                class js_Test {
                  constructor() {
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
                    import jstack.jscripter.transpiler.model.Async;
                    public abstract class Test {
                      @Async abstract void bar();
                      @Async void foo() {}
                      @Async void foo(int x) { bar(); }
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
                    import jstack.jscripter.transpiler.model.Async;
                    public class Test {
                      @Async Test() {}
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
                import jstack.jscripter.transpiler.model.Async;
                public abstract class Test {
                  @Async abstract void foo();
                  @Async void bar() { foo(); }
                }""",
            """
                class js_Test {
                  constructor() {
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
                    import jstack.jscripter.transpiler.model.Async;
                    public abstract class Test {
                      @Async abstract void foo();
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
                import jstack.jscripter.transpiler.model.Async;
                import jstack.jscripter.transpiler.model.ErasedInterface;
                public class Test {
                  @ErasedInterface
                  interface Foo {
                    @Async void foo();
                  }
                  @Async void callee(Foo f) { f.foo(); }
                }""",
            """
                class js_Test {
                  constructor() {
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
                    import jstack.jscripter.transpiler.model.Async;
                    public abstract class A {
                      @Async abstract void foo();
                    }""",
                """
                    class js_A {
                      constructor() {
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
                    import jstack.jscripter.transpiler.model.Async;
                    
                    public interface A {
                      @Async void foo();
                    }""",
                """
                    const _js_A = (superclass) => class js_A extends superclass {
                      __iface_instance__(iface) {
                        return (iface === _js_A || (typeof super.__iface_instance__ !== "undefined" && super.__iface_instance__(iface)));
                      }
                    };
                    """),
            new CompileAssert.CompileCase("js.B",
                """
                    package js;
                    class B implements A {
                      @Override public void foo() {
                      }
                    }""",
                """
                    class js_B extends _js_A(Object) {
                      constructor() {
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
                        import jstack.jscripter.transpiler.model.Async;
                        class B implements A {
                          @Async @Override public void foo() {
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
                import jstack.jscripter.transpiler.model.Async;
                import jstack.jscripter.transpiler.model.ErasedInterface;
                public abstract class Test {
                  @ErasedInterface
                  interface Foo {
                    @Async void foo();
                  }
                  @Async abstract void doo();
                  void bar() {
                    Foo instance = () -> { doo(); };
                  }
                }""",
            """
                class js_Test {
                  constructor() {
                  }
                  _bar() {
                    const instance = async () => {
                      (await this._doo());
                    };
                  }
                }
                """);
    }

    @Test void AsyncClassInitFail() throws IOException {
        try {
            assertCompiled(
                """
                    package js;
                    import jstack.jscripter.transpiler.model.Async;
                    import jstack.jscripter.transpiler.model.ErasedInterface;
                    public class Test {
                      @ErasedInterface
                      interface Foo {
                        @Async void foo();
                      }
                      Foo instance = () -> {};
                      {
                        instance.foo();
                      }
                    }""",
                """
                    class js_Test {
                      constructor() {
                        const __init__ = () => {
                          this.instance = async () => {
                          };
                        };
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
                import jstack.jscripter.transpiler.model.Async;
                import jstack.jscripter.transpiler.model.ErasedInterface;
                public class Test {
                  @ErasedInterface
                  interface Foo {
                    @Async void foo();
                  }
                  void doo() {};
                  void bar() {
                    Foo x;
                    (x = () -> { doo(); }).foo();
                  }
                }""",
            """
                class js_Test {
                  constructor() {
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
