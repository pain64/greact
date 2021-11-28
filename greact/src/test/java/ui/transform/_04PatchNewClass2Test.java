package ui.transform;

import com.over64.greact.EffectCallFinder;
import com.over64.greact.NewClassPatcher;
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

public class _04PatchNewClass2Test {
    static class PatchedLikeAssert extends AnalyzeAssertionsCompiler.CompilerAssertion<String> {
        @Override public void doAssert(Context ctx, JCTree.JCCompilationUnit cu, String expected) {
            var patcher = new NewClassPatcher2(ctx);
            var classEntries = new EffectCallFinder(ctx).find(cu);
            classEntries.forEach(patcher::patch);
            assertEquals(cu.toString(), expected);
        }
    }

    @Test void map_entry() {
        withAssert(PatchedLikeAssert.class, """
                import com.over64.greact.dom.HTMLNativeElements.*;
                class A implements Component0<div> {
                    static class Custom implements Component0<h1> {
                        final String text;
                        public Custom(String text) {this.text = text;}
                        
                        @Override public h1 mount() {
                            return new h1(text) {{
                                new h2(text) {{
                                    new h3(text);
                                }};
                            }};
                        }
                    }
                    
                    void some(div d) {}
                    
                    String theText = "hello, world!";
                    
                    @Override public div mount() {
                        return new div() {{
                            onclick = ev -> effect(theText);
                            style.color = "#eee";
                            some(new div() {{
                                new h4();
                            }});
                            new h1("hello") {{
                                for(int i = 0; i < 10; i++)
                                    new h2() {{
                                        style.color = "#ddd";
                                        new h3();
                                        new Custom(theText);
                                    }};
                            }};
                        }};
                    }
                }""",
            """
                                
                @Override
                public div mount() {
                    final com.over64.greact.dom.HTMLNativeElements.div _root = (com.over64.greact.dom.HTMLNativeElements.div)com.over64.greact.dom.GReact.element;
                    return com.over64.greact.dom.GReact.entry(()->{
                    });
                }"""
        );
    }

}
