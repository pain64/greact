package modifier.effect;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

public class Simple2 {

    @Test
    void simplestEffect2() throws IOException {
        CompileAssert.assertCompiledMany(
            new CompileAssert.CompileCase("js.Demo",
                """
                    package js;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                        
                    class Demo implements Component0<div> {
                               boolean showUsers = true;
                               String[] users = new String[]{"Ivan", "John", "Iborg"};
                       
                               @Override
                               public div mount() {
                                   return new div() {{
                                       new button() {{
                                           innerText = "toggle show users " + users.length;
                                           onclick = ev -> effect(showUsers = !showUsers);
                                       }};
                       
                                       if (showUsers)
                                           for (var user : users)
                                               new h1("name" + user);
                                       else
                                           new h1("user show disabled");
                                   }};
                               }
                    }""",
                """
                    package js;
                                       
                    import org.over64.jscripter.StdTypeConversion;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                       
                    class Demo implements Component0<div> {
                       \s
                        Demo() {
                            super();
                        }
                        boolean showUsers = true;
                        String[] users = new String[]{"Ivan", "John", "Iborg"};
                       \s
                        @Override
                        public div mount() {
                            final com.over64.greact.dom.HTMLNativeElements.div $root = (com.over64.greact.dom.HTMLNativeElements.div)com.over64.greact.dom.Globals.gReactElement;
                            return com.over64.greact.dom.Globals.gReactReturn(()->{
                                final com.over64.greact.dom.HTMLNativeElements.button $el0 = com.over64.greact.dom.Globals.document.createElement("button");
                                {
                                    $el0.innerText = "toggle show users " + users.length;
                                    $el0.onclick = (ev)->effect$showUsers(showUsers = !showUsers);
                                }
                                $root.appendChild($el0);
                                ($viewFrag0 = com.over64.greact.dom.Fragment.of(()->{
                                    $viewFrag0.cleanup();
                                    if (showUsers) for (java.lang.String user : users) {
                                        final com.over64.greact.dom.HTMLNativeElements.h1 $el1 = com.over64.greact.dom.Globals.document.createElement("h1");
                                        {
                                            $el1.innerText = "name" + user;
                                        }
                                        $viewFrag0.appendChild($el1);
                                    } else {
                                        final com.over64.greact.dom.HTMLNativeElements.h1 $el2 = com.over64.greact.dom.Globals.document.createElement("h1");
                                        {
                                            $el2.innerText = "user show disabled";
                                        }
                                        $viewFrag0.appendChild($el2);
                                    }
                                }, $root)).renderer.render();
                            });
                        }
                        private com.over64.greact.dom.Fragment $viewFrag0;
                       \s
                        private void effect$showUsers(java.lang.Object x0) {
                            $viewFrag0.renderer.render();
                        }
                    }"""));

    }
}
