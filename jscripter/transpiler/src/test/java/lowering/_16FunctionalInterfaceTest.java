package lowering;

import jstack.jscripter.transpiler.generate.util.CompileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static util.CompileAssert.assertCompiled;

public class _16FunctionalInterfaceTest {
    @Test
    void functionalInterfaceExceptionTest() {
        try {
            assertCompiled(
                """
                    package js;
                    import java.util.function.Function;

                    public class Test {
                        void aaa() {
                            Function<String, String> f = new Function<String, String>() {
                                int x = 42;
                                @Override
                                public String apply(String s) {
                                    return null;
                                }
                            };
                        }
                     }""",
                """
                    """);
        } catch (Exception ex) {
            var ce = (CompileException) ex.getCause();
            Assertions.assertSame(CompileException.ERROR.CANNOT_BE_CREATED_VIA_NEW, ce.error);
        }
    }
}
