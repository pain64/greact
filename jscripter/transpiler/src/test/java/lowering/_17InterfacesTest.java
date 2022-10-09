package lowering;

import com.greact.generate.util.CompileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;
import static util.CompileAssert.assertCompiledMany;

public class _17InterfacesTest {
    @Test void compileSimpleInterface() throws IOException {
        assertCompiled(
            """
                package js;
                                
                class Test {
                  interface InterfaceA {
                    String bar();
                    default String baz() {
                      return "baz";
                    }
                  }
                }
                """,
            """
                class js_Test {
                  constructor() {
                  }
                  const _js_Test_InterfaceA = (superclass) => class js_Test_InterfaceA extends superclass {
                    __iface_instance__(iface) {
                      return (iface === _js_Test_InterfaceA || (typeof super.__iface_instance__ !== "undefined" && super.__iface_instance__(iface)));
                    }
                    _baz() {
                      return 'baz';
                    }
                  };
                }
                """);
    }
    @Test void compileSimpleErasedInterface() throws IOException {
        assertCompiled(
            """
                package js;
                import com.greact.model.ErasedInterface;
                import com.greact.model.DoNotTranspile;
                                
                class Test {
                  @ErasedInterface
                  interface InterfaceA {
                    String bar();
                    @DoNotTranspile
                    default String baz() {
                      return "baz";
                    }
                  }
                }
                """,
            """
                class js_Test {
                  constructor() {
                  }
                }
                """);
    }
    @Test void defaultMethodInErasedInterface() {
        try {
            assertCompiled(
                """
                    package js;
                    import com.greact.model.ErasedInterface;
                                    
                    class Test {
                      @ErasedInterface
                      interface InterfaceA {
                        String bar();
                        default String baz() {
                          return "baz";
                        }
                      }
                    }""",
                """
                    """);
        } catch (Exception ex) {
            var ce = (CompileException) ex.getCause();
            Assertions.assertSame(CompileException.ERROR.THE_METHOD_MUST_BE_DECLARED_AS_DO_NOT_TRANSPILE, ce.error);
        }
    }
    @Test void interfaceExtendsOtherInterface() throws IOException {
        assertCompiled(
            """
                package js;
                                
                class Test {
                  interface InterfaceC {
                    String bar();
                    default String baz() {
                      return "baz";
                    }
                  }
                  interface InterfaceD extends InterfaceC { }
                }
                """,
            """
                class js_Test {
                  constructor() {
                  }
                  const _js_Test_InterfaceC = (superclass) => class js_Test_InterfaceC extends superclass {
                    __iface_instance__(iface) {
                      return (iface === _js_Test_InterfaceC || (typeof super.__iface_instance__ !== "undefined" && super.__iface_instance__(iface)));
                    }
                    _baz() {
                      return 'baz';
                    }
                  };
                  const _js_Test_InterfaceD = (superclass) => class js_Test_InterfaceD extends _js_Test_InterfaceC(superclass) {
                    __iface_instance__(iface) {
                      return (iface === _js_Test_InterfaceD || (typeof super.__iface_instance__ !== "undefined" && super.__iface_instance__(iface)));
                    }
                  };
                }
                """);
    }
    @Test void erasedInterfaceInheritedFromOtherInterface() {
        try {
            assertCompiled(
                """
                    package js;
                    import com.greact.model.ErasedInterface;
                    import com.greact.model.DoNotTranspile;
                                        
                    class Test {
                      interface A { }
                      @ErasedInterface
                      interface InterfaceA extends A {
                        String bar();
                        @DoNotTranspile
                        default String baz() {
                          return "baz";
                        }
                      }
                    }""",
                """
                    """);
        } catch (Exception ex) {
            var ce = (CompileException) ex.getCause();
            Assertions.assertSame(CompileException.ERROR.ERASED_INTERFACE_CAN_BE_INHERITED_ONLY_FROM_ERASED_INTERFACE, ce.error);
        }
    }
    @Test void erasedInterfaceInheritedFromErasedInterface() {
        try {
            assertCompiled(
                """
                    package js;
                    import com.greact.model.ErasedInterface;
                    import com.greact.model.DoNotTranspile;
                                        
                    class Test {
                      @ErasedInterface
                      interface InterfaceA {
                        String bar();
                        @DoNotTranspile
                        default String baz() {
                          return "baz";
                        }
                      }
                      interface B extends InterfaceA { }
                    }""",
                """
                    """);
        } catch (Exception ex) {
            var ce = (CompileException) ex.getCause();
            Assertions.assertSame(CompileException.ERROR.ERASED_INTERFACE_CAN_BE_INHERITED_ONLY_FROM_ERASED_INTERFACE, ce.error);
        }
    }
    @Test void instanceOfForErasedInterface() {
        try {
            assertCompiled(
                """
                    package js;
                    import com.greact.model.ErasedInterface;
                    import com.greact.model.DoNotTranspile;
                                        
                    class Test {
                      @ErasedInterface
                      interface InterfaceA {
                        String bar();
                        @DoNotTranspile
                        default String baz() {
                          return "baz";
                        }
                      }
                      void baz(Object x) {
                        boolean y1 = x instanceof InterfaceA;
                      }
                    }""",
                """
                    """);
        } catch (Exception ex) {
            var ce = (CompileException) ex.getCause();
            Assertions.assertSame(CompileException.ERROR.ERASED_INTERFACE_NOT_USE_OPERATOR_INSTANCE_OF, ce.error);
        }
    }
    @Test void classImplementingInterfaces() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.A",
                """
                    package js;
                    class A {}""",
                """
                    class js_A {
                      constructor() {
                      }
                    }
                    """),
            new CompileAssert.CompileCase("js.InterfaceA",
                """
                    package js;
                    interface InterfaceA {
                      public void bar();
                    }""",
                """
                    const _js_InterfaceA = (superclass) => class js_InterfaceA extends superclass {
                      __iface_instance__(iface) {
                        return (iface === _js_InterfaceA || (typeof super.__iface_instance__ !== "undefined" && super.__iface_instance__(iface)));
                      }
                    };
                    """),
            new CompileAssert.CompileCase("js.InterfaceB",
                """
                    package js;
                    interface InterfaceB {
                      public void foo();
                    }""",
                """
                    const _js_InterfaceB = (superclass) => class js_InterfaceB extends superclass {
                      __iface_instance__(iface) {
                        return (iface === _js_InterfaceB || (typeof super.__iface_instance__ !== "undefined" && super.__iface_instance__(iface)));
                      }
                    };
                    """),
            new CompileAssert.CompileCase("js.Test", """
                package js;
                                
                class Test extends A implements InterfaceA, InterfaceB {
                  @Override public void bar() { return; }
                  @Override public void foo() { return; }
                }
                """,
                """
                    class js_Test extends js_InterfaceA(js_InterfaceB(js_A)) {
                      constructor() {
                        super();
                      }
                      _bar() {
                        return;
                      }
                      _foo() {
                        return;
                      }
                    }
                    """));
    }
    @Test void implementErasedInterface() {
        try {
            assertCompiled(
                """
                    package js;
                    import com.greact.model.ErasedInterface;
                    import com.greact.model.DoNotTranspile;
                                        
                    class Test {
                      @ErasedInterface
                      interface InterfaceA {
                        String bar();
                        @DoNotTranspile
                        default String baz() {
                          return "baz";
                        }
                      }
                      static class A implements InterfaceA { }
                    }""",
                """
                    """);
        } catch (Exception ex) {
            var ce = (CompileException) ex.getCause();
            Assertions.assertSame(CompileException.ERROR.CLASS_CANNOT_BE_INHERITED_FROM_ERASED_INTERFACE, ce.error);
        }
    }
    @Test void instanceOfForInterface() throws IOException {
        assertCompiled("""
            package js;
                                
                class Test {
                  class ClassC {}
                  interface InterfaceC {
                    String bar();
                    default String baz() {
                      return "baz";
                    }
                  }
                }
            """, """
                        
            """);
    }
}
