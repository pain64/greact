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
            new NewClassPatcher2(ctx).patch(cu);
            assertEquals(cu.toString(), expected);
        }
    }

    @Test void map_entry() {
        withAssert(PatchedLikeAssert.class, """
                import com.over64.greact.dom.HTMLNativeElements.*;
                class A implements Component0<div> {
                    void some(div d) {}
                    
                    @Override public div mount() {
                        return new div() {{
                            some(new div() {{
                                new h4();
                            }});
                            new h1() {{
                                for(int i = 0; i < 10; i++)
                                    new h2() {{
                                        new h3();
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
