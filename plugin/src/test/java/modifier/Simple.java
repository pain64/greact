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
                    import com.over64.greact.dom.HTMLNativeElements.*;
                    import com.over64.greact.model.components.Component;
                    import com.over64.greact.dom.HtmlElement;
                    
                    class Demo implements Component {
                      @Override public void mount(HtmlElement dom) {                        
                        GReact.mount(dom, new div() {{
                          className = "my-div";
                          new h1() {{
                            innerText = "hello, GReact";
                          }};
                        }});
                      }
                    }""",
                """
                    package js;
                                        
                    import com.over64.greact.GReact;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                    import com.over64.greact.model.components.Component;
                    import com.over64.greact.dom.HtmlElement;
                                        
                    class Demo implements Component {
                       \s
                        Demo() {
                            super();
                        }
                       \s
                        @Override
                        public void mount(HtmlElement dom) {
                            GReact.mount(dom, new div(){
                               \s
                                () {
                                    super();
                                }
                                {
                                    className = "my-div";
                                    new h1("hello, GReact");
                                }
                            });
                        }
                    }"""));

    }
}
