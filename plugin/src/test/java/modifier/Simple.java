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
                    import org.over64.jscripter.std.js.DocumentFragment;
                    class Demo {
                      Demo(DocumentFragment dom) {
                        com.over64.greact.GReact.render(dom, "<H1 text=\\"hello, GReact\\" />", H1.class);
                      }
                    }""",
                """
                    """),
            new CompileAssert.CompileCase("js.H1",
                """
                    package js;
                    import org.over64.jscripter.std.js.DocumentFragment;
                    class H1 {
                      H1(DocumentFragment dom, String text) {
                      }
                    }""",
                """
                    """));

    }
}
