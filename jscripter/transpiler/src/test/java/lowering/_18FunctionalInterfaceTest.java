package lowering;

import jstack.jscripter.transpiler.generate.util.CompileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;

public class _18FunctionalInterfaceTest {
    @Test void compileSimpleFunctionalInterface() throws IOException {
        assertCompiled(
            """
                package js;
                                
                class Test {
                  @FunctionalInterface
                  interface InterfaceA {
                    void bar();
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

    @Test void compileFunctionalInterfaceWithDefaultMethod() {
        try {
            assertCompiled(
                """
                    package js;
                                    
                    class Test {
                      @FunctionalInterface
                      interface InterfaceA {
                        default void bar() {}
                      }
                    }
                    """,
                """
                    """);
        } catch (Exception ex) {
            var ce = (CompileException) ex.getCause();
            Assertions.assertSame(CompileException.ERROR.THE_METHOD_CANNOT_BE_DECLARED_DEFAULT, ce.error);
        }
    }

    @Test void compileFunctionalInterfaceExtendsOtherInterface() {
        try {
            assertCompiled(
                """
                    package js;
                                    
                    class Test {
                      interface interfaceA {}
                      
                      @FunctionalInterface
                      interface InterfaceB extends interfaceA {
                        void bar();
                      }
                    }
                    """,
                """
                    """);
        } catch (Exception ex) {
            var ce = (CompileException) ex.getCause();
            Assertions.assertSame(CompileException.ERROR.FUNCTIONAL_INTERFACE_CAN_EXTEND_ONLY_FUNCTIONAL_INTERFACE, ce.error);
        }
    }
}
