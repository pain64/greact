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
                import com.greact.model.CSS;
                                    
                @CSS.Require({"test.css"}) class Test {}
                """,
            """
                class js$Test extends Object {
                  constructor() {
                    super();
                  }
                }
                """);
    }
}