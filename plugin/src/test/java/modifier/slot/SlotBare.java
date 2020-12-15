package modifier.slot;

import org.junit.jupiter.api.Test;
import util.CompileAssert;

import java.io.IOException;

import static util.CompileAssert.assertCompiledMany;

public class SlotBare {
    @Test void slotBare() throws IOException {
        assertCompiledMany(
            new CompileAssert.CompileCase("js.Conditional",
                """
                    package js;
                                   
                    import com.greact.model.JSExpression;
                    import com.over64.greact.dom.HTMLNativeElements.*;
                    import com.over64.greact.dom.HtmlElement;
                    import com.over64.greact.model.components.Component;
                                                  
                    class Conditional<T extends HtmlElement> implements Component<T> {
                        boolean cond = false;
                        Component<T> doThen = () -> null;
                        Component<T> doElse = () -> null;
                                                              
                        T call(Component<T> comp) {
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
                    import com.over64.greact.model.components.Component;
                                        
                    class Conditional<T extends HtmlElement> implements Component<T> {
                       \s
                        Conditional() {
                            super();
                        }
                        boolean cond = false;
                        Component<T> doThen = ()->null;
                        Component<T> doElse = ()->null;
                       \s
                        T call(Component<T> comp) {
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
                    import com.over64.greact.model.components.Component;
                                   
                                   
                    public class Demo implements Component<div> {                                   
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
                                    onclick = () -> effect(showHint = !showHint);
                                }};
                            }};
                        }
                    }""",
                ""));
    }
}
