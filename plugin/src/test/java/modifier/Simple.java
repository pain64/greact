package modifier;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

public class Simple {
    @Test void foo() throws IOException {
        CompileAssert.assertCompiledMany(
            new CompileAssert.CompileCase("js.Demo",
                """
                    package js;
                    //import static com.over64.greact.GReact.render;
                    class Demo {
                      Demo() {
                        com.over64.greact.GReact.render("<H1 text=\\"hello, GReact\\" />", H1.class);
                      }
                    }""",
                """
                    """),
            new CompileAssert.CompileCase("js.H1",
                """
                    package js;
                    class H1 {
                      H1(String text) {
                      }
                    }""",
                """
                    """));

    }
}
