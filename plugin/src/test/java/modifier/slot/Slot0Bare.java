package modifier.slot;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

import static util.CompileAssert.assertCompiledMany;

public class Slot0Bare {
    @Test void slotBare() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.Conditional",
                """
                    package js;
                                   
                    import com.greact.model.JSExpression;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                    import com.over64.greact.dom.HtmlElement;
                                                  
                    class Conditional<T extends HtmlElement> implements Component0<T> {
                        boolean cond = false;
                        Component0<T> doThen = () -> null;
                        Component0<T> doElse = () -> null;
                                                              
                        T call(Component0<T> comp) {
                            return JSExpression.of("comp instanceof Function ? comp() : comp.mount()");
                        }
                               
                        @Override
                        public T mount() {
                            return cond ? call(doThen) : call(doElse);
                        }
                    }""",
                """
                    package js;
                                        
                    import org.over64.jscripter.StdTypeConversion;
                    import com.greact.model.JSExpression;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                    import com.over64.greact.dom.HtmlElement;
                                        
                    class Conditional<T extends HtmlElement> implements Component0<T> {
                       \s
                        Conditional() {
                            super();
                        }
                        boolean cond = false;
                        Component0<T> doThen = ()->null;
                        Component0<T> doElse = ()->null;
                       \s
                        T call(Component0<T> comp) {
                            return JSExpression.of("comp instanceof Function ? comp() : comp.mount()");
                        }
                       \s
                        @Override
                        public T mount() {
                            final T $root = (T)com.over64.greact.dom.Globals.gReactElement;
                            return cond ? call(doThen) : call(doElse);
                        }
                    }"""),
            new CompileAssert.CompileCase("js.Demo",
                """
                    package js;
                                   
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                   
                    public class Demo implements Component0<div> {                                   
                        boolean showHint = true;
                                   
                        @Override
                        public div mount() {
                            return new div() {{
                                new Conditional<h1>() {{
                                    cond = showHint;
                                    doThen = () -> new h1("This is the hint");
                                    doElse = () -> new h1("The hint is hidden");
                                }};
                                new button("show/hide") {{
                                    onclick = ev -> effect(showHint = !showHint);
                                }};
                            }};
                        }
                    }""",
                """
                    package js;
                                       
                    import org.over64.jscripter.StdTypeConversion;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                                       
                    public class Demo implements Component0<div> {
                       \s
                        public Demo() {
                            super();
                        }
                        boolean showHint = true;
                       \s
                        @Override
                        public div mount() {
                            final com.over64.greact.dom.HTMLNativeElements.div $root = (com.over64.greact.dom.HTMLNativeElements.div)com.over64.greact.dom.Globals.gReactElement;
                            return com.over64.greact.dom.Globals.gReactReturn(()->{
                                ($viewFrag0 = com.over64.greact.dom.Fragment.of(()->{
                                    $viewFrag0.cleanup();
                                    final com.over64.greact.dom.HTMLNativeElements.h1 $el0 = com.over64.greact.dom.Globals.document.createElement("h1");
                                    final js.Conditional<com.over64.greact.dom.HTMLNativeElements.h1> $comp0 = new Conditional<h1>();
                                    {
                                        $comp0.cond = showHint;
                                        $comp0.doThen = ()->{
                                            final com.over64.greact.dom.HTMLNativeElements.h1 $root = (com.over64.greact.dom.HTMLNativeElements.h1)com.over64.greact.dom.Globals.gReactElement;
                                            return com.over64.greact.dom.Globals.gReactReturn(()->{
                                                $root.innerText = "This is the hint";
                                            });
                                        };
                                        $comp0.doElse = ()->{
                                            final com.over64.greact.dom.HTMLNativeElements.h1 $root = (com.over64.greact.dom.HTMLNativeElements.h1)com.over64.greact.dom.Globals.gReactElement;
                                            return com.over64.greact.dom.Globals.gReactReturn(()->{
                                                $root.innerText = "The hint is hidden";
                                            });
                                        };
                                    }
                                    com.over64.greact.dom.Globals.gReactMount($el0, $comp0, new Object[]{});
                                    $viewFrag0.appendChild($el0);
                                }, $root)).renderer.render();
                                final com.over64.greact.dom.HTMLNativeElements.button $el1 = com.over64.greact.dom.Globals.document.createElement("button");
                                {
                                    $el1.innerText = "show/hide";
                                    $el1.onclick = (ev)->effect$showHint(showHint = !showHint);
                                }
                                $root.appendChild($el1);
                            });
                        }
                        private com.over64.greact.dom.Fragment $viewFrag0;
                       \s
                        private void effect$showHint(java.lang.Object x0) {
                            $viewFrag0.renderer.render();
                        }
                    }"""));
    }
}
