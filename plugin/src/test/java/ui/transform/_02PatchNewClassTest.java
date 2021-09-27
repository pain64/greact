package ui.transform;

import com.over64.greact.EffectCallFinder;
import com.over64.greact.NewClassPatcher;
import com.over64.greact.ViewEntryFinder;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import org.junit.jupiter.api.Test;
import util.AnalyzeAssertionsCompiler;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static util.AnalyzeAssertionsCompiler.withAssert;

public class _02PatchNewClassTest {
    static class PatchedLikeAssert extends AnalyzeAssertionsCompiler.CompilerAssertion<String[]> {
        @Override public void doAssert(Context ctx, JCTree.JCCompilationUnit cu, String[] expected) {
            var effectMap = new EffectCallFinder(ctx).find(cu);
            var has = new ViewEntryFinder(ctx).find(cu).stream()
                .flatMap(ce -> {
                    var effectsForClass = effectMap.getOrDefault(ce.classDecl(), new ArrayList<>());
                    new NewClassPatcher(ctx).patch(ce, effectsForClass);
                    return ce.viewHolders().stream().map(vh -> vh.target().toString());
                }).toArray(String[]::new);

            assertArrayEquals(expected, has);
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
            new String[]{"""
                                
                @Override
                public div mount() {
                    final com.over64.greact.dom.HTMLNativeElements.div _root = (com.over64.greact.dom.HTMLNativeElements.div)com.over64.greact.dom.GReact.element;
                    return com.over64.greact.dom.GReact.entry(()->{
                    });
                }"""
            });
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
            new String[]{"""
                                
                @Override
                public div mount() {
                    final com.over64.greact.dom.HTMLNativeElements.div _root = (com.over64.greact.dom.HTMLNativeElements.div)com.over64.greact.dom.GReact.element;
                    return com.over64.greact.dom.GReact.entry(()->{
                        _root.innerText = "hello";
                        _root.innerText = "world";
                    });
                }"""
            });
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
            new String[]{"""
                                
                @Override
                public div mount() {
                    final com.over64.greact.dom.HTMLNativeElements.div _root = (com.over64.greact.dom.HTMLNativeElements.div)com.over64.greact.dom.GReact.element;
                    return com.over64.greact.dom.GReact.entry(()->{
                        com.over64.greact.dom.GReact.make(_root, "h1", (com.over64.greact.dom.HTMLNativeElements.h1 _el0)->{
                            _el0.innerText = "hello";
                            com.over64.greact.dom.GReact.make(_el0, "h2", (com.over64.greact.dom.HTMLNativeElements.h2 _el1)->{
                                _el1.innerText = "world";
                            });
                        });
                    });
                }"""
            });
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
            new String[]{"""
                                
                @Override
                public h1 mount() {
                    final com.over64.greact.dom.HTMLNativeElements.h1 _root = (com.over64.greact.dom.HTMLNativeElements.h1)com.over64.greact.dom.GReact.element;
                    return com.over64.greact.dom.GReact.entry(()->{
                        _root.innerText = "hello";
                        com.over64.greact.dom.GReact.make(_root, "h1", (com.over64.greact.dom.HTMLNativeElements.h1 _el0)->{
                            _el0.innerText = "world";
                        });
                    });
                }"""
            });
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
            new String[]{
                """
                
                @Override
                public Child mount() {
                    final com.over64.greact.dom.HTMLNativeElements.h1 _root = (com.over64.greact.dom.HTMLNativeElements.h1)com.over64.greact.dom.GReact.element;
                    return com.over64.greact.dom.GReact.entry(()->{
                        com.over64.greact.dom.GReact.mount(_root, new Child(), new Object[]{});
                    });
                }""",
                """
                
                @Override
                public h1 mount() {
                    final com.over64.greact.dom.HTMLNativeElements.h1 _root = (com.over64.greact.dom.HTMLNativeElements.h1)com.over64.greact.dom.GReact.element;
                    return com.over64.greact.dom.GReact.entry(()->{
                        _root.innerText = "hello";
                    });
                }"""
            });
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
            new String[]{
                """
                (s)->{
                    final com.over64.greact.dom.HTMLNativeElements.h1 _root = (com.over64.greact.dom.HTMLNativeElements.h1)com.over64.greact.dom.GReact.element;
                    return com.over64.greact.dom.GReact.entry(()->{
                        _root.innerText = s + "hello";
                    });
                }""",
                """
                
                @Override
                public div mount() {
                    final com.over64.greact.dom.HTMLNativeElements.div _root = (com.over64.greact.dom.HTMLNativeElements.div)com.over64.greact.dom.GReact.element;
                    return com.over64.greact.dom.GReact.entry(()->{
                        _root.style.border = "1px red solid";
                        for (java.lang.String s : list) {
                            com.over64.greact.dom.GReact.make(_root, "div", (com.over64.greact.dom.HTMLNativeElements.div _el0)->{
                                _el0.style.border = "1px green solid";
                                com.over64.greact.dom.GReact.make(_el0, "h1", (com.over64.greact.dom.HTMLNativeElements.h1 _el1)->{
                                    com.over64.greact.dom.GReact.mount(_el1, forDecorate, new Object[]{s});
                                });
                            });
                        }
                    });
                }"""
            });
    }

    /* FIXME:
        1. also support slot as root
        2. check that new slot<>() is parametrised (has <>) */
}