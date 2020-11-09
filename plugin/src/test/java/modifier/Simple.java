package modifier;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

public class Simple {
    @Test void foo() throws IOException {

//        import static com.over64.greact.GReact.effect;
//import com.over64.greact.GReact;
//import static com.over64.greact.GReact.render;
//
//        public class DemoAdvanced {
//            enum Mode {M1, M2}
//
//            com.over64.greact.sample.DemoAdvanced.Mode mode = com.over64.greact.sample.DemoAdvanced.Mode.M1;
//            boolean showUsers = true;
//
//            public DemoAdvanced(DocumentFragment dom) {
//                var users = new String[]{"Ivan", "John", "Iborg"};
//
//                Runnable toggle = () -> effect(showUsers = !showUsers);
//                GReact.render(dom, """
//            <h1>hello</h1>""");
//
//                com.over64.greact.GReact.render(dom, """
//            <h1>hello</h1>""");
//
//
                CompileAssert.assertCompiledMany(
            new CompileAssert.CompileCase("js.Demo",
                """
                    package js;
                    import com.over64.greact.GReact;
                    import static com.over64.greact.GReact.render;
                    import org.over64.jscripter.std.js.DocumentFragment;
                    
                    class Demo {
                      Demo(DocumentFragment dom) {
                        render(dom, "<H1 text=\\"hello, GReact\\" />", H1.class);
                        GReact.render(dom, "<H1 text=\\"hello, GReact\\" />", H1.class);
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
