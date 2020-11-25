package modifier.mount;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

public class Simple {
    @Test void foo() throws IOException {
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
                          fake.className = "123";
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
