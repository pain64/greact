package lowering;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static util.CompileAssert.assertCompiled;

public class _14Bundle {
    @Test public void ccsBundleTest() throws IOException {
        /*
          FIXME: пока непонятно как на уровне javac проверять наличие css файлов, которые мы включаем
         */
        assertCompiled(
            """
                package js;
                import jstack.jscripter.transpiler.model.Require;
                                    
                @Require.CSS({"test.css"}) class Test {}
                """,
            """
                class js_Test {
                  constructor() {
                  }
                }
                """);
    }
}