package ui.transform;

import jstack.greact.EffectCallFinder;
import jstack.greact.NewClassPatcher2;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import org.junit.jupiter.api.Test;
import util.AnalyzeAssertionsCompiler;

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
                import jstack.greact.html.*;
                class A implements Component0<div> {
                    @Override public div mount() {
                        return new div();
                    }
                }""",
            """
                
                import jstack.greact.html.*;
                  
                class A implements Component0<div> {
                   \s
                    A() {
                        super();
                    }
                   \s
                    @Override
                    public div mount() {
                        return (()->{
                            final jstack.greact.html.div _el0 = jstack.jscripter.transpiler.model.JSExpression.of("document.createElement(\\'div\\')");
                            return _el0;
                        }).call();
                    }
                }""");
    }

    @Test void map_as_local_expression() {
        withAssert(PatchedLikeAssert.class, """
                import jstack.greact.html.*;
                class A implements Component0<div> {
                    @Override public div mount() {
                        var b = new button("lol");
                        return new div();
                    }
                }""",
            """    
                
                import jstack.greact.html.*;
                                 
                class A implements Component0<div> {
                   \s
                    A() {
                        super();
                    }
                   \s
                    @Override
                    public div mount() {
                        jstack.greact.html.button b = (()->{
                            final jstack.greact.html.button _el0 = jstack.jscripter.transpiler.model.JSExpression.of("document.createElement(\\'button\\')");
                            _el0.innerText = "lol";
                            return _el0;
                        }).call();
                        return (()->{
                            final jstack.greact.html.div _el1 = jstack.jscripter.transpiler.model.JSExpression.of("document.createElement(\\'div\\')");
                            return _el1;
                        }).call();
                    }
                }""");
    }

    @Test void map_as_new_class_argument() {
        withAssert(PatchedLikeAssert.class, """
                import jstack.greact.html.*;
                class A implements Component0<div> {
                    static class B implements Component0<div> {
                        final h1 some;
                        B(h1 some) {this.some = some;}
                        
                        @Override public div mount() {
                            return new div();
                        }
                    }
                    @Override public B mount() {
                        return new B(new h1());
                    }
                }""",
            """    
                                      
                import jstack.greact.html.*;
                                 
                class A implements Component0<div> {
                   \s
                    A() {
                        super();
                    }
                   \s
                    static class B implements Component0<div> {
                        final h1 some;
                       \s
                        B(h1 some) {
                            super();
                            this.some = some;
                        }
                       \s
                        @Override
                        public div mount() {
                            return (()->{
                                final jstack.greact.html.div _el0 = jstack.jscripter.transpiler.model.JSExpression.of("document.createElement(\\'div\\')");
                                return _el0;
                            }).call();
                        }
                    }
                   \s
                    @Override
                    public B mount() {
                        return new B((()->{
                            final jstack.greact.html.h1 _el1 = jstack.jscripter.transpiler.model.JSExpression.of("document.createElement(\\'h1\\')");
                            return _el1;
                        }).call());
                    }
                }""");
    }

    @Test void map_new_html_element_this_to_element_name() {
        withAssert(PatchedLikeAssert.class, """
                import jstack.greact.html.*;
                class A implements Component0<div> {
                    @Override public div mount() {
                        return new div() {{
                            this.innerText = "hello";
                            innerText = "world";
                        }};
                    }
                }""",
            """
                                
                import jstack.greact.html.*;
                                
                class A implements Component0<div> {
                   \s
                    A() {
                        super();
                    }
                   \s
                    @Override
                    public div mount() {
                        return (()->{
                            final jstack.greact.html.div _el0 = jstack.jscripter.transpiler.model.JSExpression.of("document.createElement(\\'div\\')");
                            _el0.innerText = "hello";
                            _el0.innerText = "world";
                            return _el0;
                        }).call();
                    }
                }""");
    }

    @Test void map_new_html_element_to_GReact_make() {
        withAssert(PatchedLikeAssert.class, """
                import jstack.greact.html.*;
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
                                
                import jstack.greact.html.*;
                                
                class A implements Component0<div> {
                   \s
                    A() {
                        super();
                    }
                   \s
                    @Override
                    public div mount() {
                        return (()->{
                            final jstack.greact.html.div _el0 = jstack.jscripter.transpiler.model.JSExpression.of("document.createElement(\\'div\\')");
                            {
                                final jstack.greact.html.h1 _el1 = jstack.jscripter.transpiler.model.JSExpression.of("document.createElement(\\'h1\\')");
                                _el1.innerText = "hello";
                                {
                                    final jstack.greact.html.h2 _el2 = jstack.jscripter.transpiler.model.JSExpression.of("document.createElement(\\'h2\\')");
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
                import jstack.greact.html.*;
                class A implements Component0<h1> {
                    @Override public h1 mount() {
                        return new h1("hello") {{
                            new h1("world");
                        }};
                    }
                }""",
            """
                                
                import jstack.greact.html.*;
                                
                class A implements Component0<h1> {
                   \s
                    A() {
                        super();
                    }
                   \s
                    @Override
                    public h1 mount() {
                        return (()->{
                            final jstack.greact.html.h1 _el0 = jstack.jscripter.transpiler.model.JSExpression.of("document.createElement(\\'h1\\')");
                            _el0.innerText = "hello";
                            {
                                final jstack.greact.html.h1 _el1 = jstack.jscripter.transpiler.model.JSExpression.of("document.createElement(\\'h1\\')");
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
            import jstack.greact.html.*;
                                
            class Demo implements Component0<h1> {
              static class Child implements Component0<h1> {
                @Override public h1 mount() { return new h1("hello"); }
              }
              
              @Override public Child mount() { return new Child(); }
            }""",
            """
                                
                import jstack.greact.html.*;
                                
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
                                final jstack.greact.html.h1 _el0 = jstack.jscripter.transpiler.model.JSExpression.of("document.createElement(\\'h1\\')");
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
                import jstack.greact.html.*;
                                   
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
                                
                import jstack.greact.html.*;
                                
                class ListDecorator implements Component0<div> {
                    final String[] list;
                    Component1<h1, String> forDecorate = (s)->(()->{
                        final jstack.greact.html.h1 _el0 = jstack.jscripter.transpiler.model.JSExpression.of("document.createElement(\\'h1\\')");
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
                            final jstack.greact.html.div _el1 = jstack.jscripter.transpiler.model.JSExpression.of("document.createElement(\\'div\\')");
                            _el1.style.border = "1px red solid";
                            for (java.lang.String s : list) {
                                {
                                    final jstack.greact.html.div _el2 = jstack.jscripter.transpiler.model.JSExpression.of("document.createElement(\\'div\\')");
                                    _el2.style.border = "1px green solid";
                                    jstack.greact.dom.GReact.mmount(_el2, forDecorate, s);
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