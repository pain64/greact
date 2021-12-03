package ui.transform;

import com.over64.greact.EffectCallFinder;
import com.over64.greact.NewClassPatcher2;
import com.over64.greact.ViewEntryFinder;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import org.junit.jupiter.api.Test;
import util.AnalyzeAssertionsCompiler;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static util.AnalyzeAssertionsCompiler.withAssert;

public class _01PatchNewClassTest {
    static class PatchedLikeAssert extends AnalyzeAssertionsCompiler.CompilerAssertion<String> {
        @Override public void doAssert(Context ctx, JCTree.JCCompilationUnit cu, String expected) {
            var patcher = new NewClassPatcher2(ctx);
            var classEntries = new EffectCallFinder(ctx).find(cu);
            classEntries.forEach(patcher::patch);

            assertEquals(expected, cu.toString());
        }
    }

    @Test void map_entry() {
        withAssert(PatchedLikeAssert.class, """
                import com.over64.greact.dom.HTMLNativeElements.*;
                class A implements Component0<div> {
                    @Override public div mount() {
                        return new div();
                    }
                }""",
            """
                
                import com.over64.greact.dom.HTMLNativeElements.*;
                  
                class A implements Component0<div> {
                   \s
                    A() {
                        super();
                    }
                   \s
                    @Override
                    public div mount() {
                        return (()->{
                            final com.over64.greact.dom.HTMLNativeElements.div _el0 = com.greact.model.JSExpression.of("document.createElement(\\'div\\')");
                            return _el0;
                        }).call();
                    }
                }""");
    }

    @Test void map_new_html_element_this_to_element_name() {
        withAssert(PatchedLikeAssert.class, """
                import com.over64.greact.dom.HTMLNativeElements.*;
                class A implements Component0<div> {
                    @Override public div mount() {
                        return new div() {{
                            this.innerText = "hello";
                            innerText = "world";
                        }};
                    }
                }""",
            """
                                
                import com.over64.greact.dom.HTMLNativeElements.*;
                                
                class A implements Component0<div> {
                   \s
                    A() {
                        super();
                    }
                   \s
                    @Override
                    public div mount() {
                        return (()->{
                            final com.over64.greact.dom.HTMLNativeElements.div _el0 = com.greact.model.JSExpression.of("document.createElement(\\'div\\')");
                            _el0.innerText = "hello";
                            _el0.innerText = "world";
                            return _el0;
                        }).call();
                    }
                }""");
    }

    @Test void map_new_html_element_to_GReact_make() {
        withAssert(PatchedLikeAssert.class, """
                import com.over64.greact.dom.HTMLNativeElements.*;
                class A implements Component0<div> {
                    @Override public div mount() {
                        return new div() {{
                            new h1() {{
                                innerText = "hello";
                                new h2() {{
                                    innerText = "world";
                                }};
                            }};
                        }};
                    }
                }""",
            """
                                
                import com.over64.greact.dom.HTMLNativeElements.*;
                                
                class A implements Component0<div> {
                   \s
                    A() {
                        super();
                    }
                   \s
                    @Override
                    public div mount() {
                        return (()->{
                            final com.over64.greact.dom.HTMLNativeElements.div _el0 = com.greact.model.JSExpression.of("document.createElement(\\'div\\')");
                            {
                                final com.over64.greact.dom.HTMLNativeElements.h1 _el1 = com.greact.model.JSExpression.of("document.createElement(\\'h1\\')");
                                _el1.innerText = "hello";
                                {
                                    final com.over64.greact.dom.HTMLNativeElements.h2 _el2 = com.greact.model.JSExpression.of("document.createElement(\\'h2\\')");
                                    _el2.innerText = "world";
                                    _el1.appendChild(_el2);
                                }
                                _el0.appendChild(_el1);
                            }
                            return _el0;
                        }).call();
                    }
                }""");
    }

    @Test void map_new_html_element_args_to_statements() {
        withAssert(PatchedLikeAssert.class, """
                import com.over64.greact.dom.HTMLNativeElements.*;
                class A implements Component0<h1> {
                    @Override public h1 mount() {
                        return new h1("hello") {{
                            new h1("world");
                        }};
                    }
                }""",
            """
                                
                import com.over64.greact.dom.HTMLNativeElements.*;
                                
                class A implements Component0<h1> {
                   \s
                    A() {
                        super();
                    }
                   \s
                    @Override
                    public h1 mount() {
                        return (()->{
                            final com.over64.greact.dom.HTMLNativeElements.h1 _el0 = com.greact.model.JSExpression.of("document.createElement(\\'h1\\')");
                            _el0.innerText = "hello";
                            {
                                final com.over64.greact.dom.HTMLNativeElements.h1 _el1 = com.greact.model.JSExpression.of("document.createElement(\\'h1\\')");
                                _el1.innerText = "world";
                                _el0.appendChild(_el1);
                            }
                            return _el0;
                        }).call();
                    }
                }""");
    }

    @Test void map_new_custom_component_to_GReact_mount() {
        withAssert(PatchedLikeAssert.class, """
            import com.over64.greact.dom.HTMLNativeElements.*;
                                
            class Demo implements Component0<h1> {
              static class Child implements Component0<h1> {
                @Override public h1 mount() { return new h1("hello"); }
              }
              
              @Override public Child mount() { return new Child(); }
            }""",
            """
                                
                import com.over64.greact.dom.HTMLNativeElements.*;
                                
                class Demo implements Component0<h1> {
                   \s
                    Demo() {
                        super();
                    }
                   \s
                    static class Child implements Component0<h1> {
                       \s
                        Child() {
                            super();
                        }
                       \s
                        @Override
                        public h1 mount() {
                            return (()->{
                                final com.over64.greact.dom.HTMLNativeElements.h1 _el0 = com.greact.model.JSExpression.of("document.createElement(\\'h1\\')");
                                _el0.innerText = "hello";
                                return _el0;
                            }).call();
                        }
                    }
                   \s
                    @Override
                    public Child mount() {
                        return new Child();
                    }
                }""");
    }

    @Test void map_new_slot_to_GReact_mountSlot() {
        withAssert(PatchedLikeAssert.class, """
                import com.over64.greact.dom.HTMLNativeElements.*;
                                   
                class ListDecorator implements Component0<div> {
                    final String[] list;
                    Component1<h1, String> forDecorate = s -> new h1(s + "hello");
                    
                    ListDecorator(String[] list) { this.list = list; }
                    
                    @Override public div mount() {
                        return new div() {{
                            style.border = "1px red solid";
                            for (var s : list) {
                                new div() {{
                                    style.border = "1px green solid";
                                    new slot<>(forDecorate, s);
                                }};
                            }
                        }};
                    }
                }""",
            """
                                
                import com.over64.greact.dom.HTMLNativeElements.*;
                                
                class ListDecorator implements Component0<div> {
                    final String[] list;
                    Component1<h1, String> forDecorate = (s)->(()->{
                        final com.over64.greact.dom.HTMLNativeElements.h1 _el0 = com.greact.model.JSExpression.of("document.createElement(\\'h1\\')");
                        _el0.innerText = s + "hello";
                        return _el0;
                    }).call();
                   \s
                    ListDecorator(String[] list) {
                        super();
                        this.list = list;
                    }
                   \s
                    @Override
                    public div mount() {
                        return (()->{
                            final com.over64.greact.dom.HTMLNativeElements.div _el1 = com.greact.model.JSExpression.of("document.createElement(\\'div\\')");
                            _el1.style.border = "1px red solid";
                            for (java.lang.String s : list) {
                                {
                                    final com.over64.greact.dom.HTMLNativeElements.div _el2 = com.greact.model.JSExpression.of("document.createElement(\\'div\\')");
                                    _el2.style.border = "1px green solid";
                                    com.over64.greact.dom.GReact.mmount(_el2, forDecorate, new Object[]{s});
                                    _el1.appendChild(_el2);
                                }
                            }
                            return _el1;
                        }).call();
                    }
                }""");
    }

    /* FIXME: 1. also support slot as root */
}