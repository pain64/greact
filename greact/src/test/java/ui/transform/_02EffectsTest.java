package ui.transform;

import com.over64.greact.EffectCallFinder;
import com.over64.greact.NewClassPatcher2;
import com.over64.greact.ViewEntryFinder;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import org.junit.jupiter.api.Test;
import util.AnalyzeAssertionsCompiler;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static util.AnalyzeAssertionsCompiler.withAssert;

public class _02EffectsTest {

    static class PatchedLikeAssert extends AnalyzeAssertionsCompiler.CompilerAssertion<String> {
        @Override public void doAssert(Context ctx, JCTree.JCCompilationUnit cu, String expected) {
            var patcher = new NewClassPatcher2(ctx);
            var classEntries = new EffectCallFinder(ctx).find(cu);
            classEntries.forEach(patcher::patch);

            assertEquals(expected, cu.toString());
        }
    }

    @Test void simple() {
        withAssert(PatchedLikeAssert.class, """
                import com.over64.greact.dom.HTMLNativeElements.*;
                class A implements Component0<div> {
                    int n = 0;
                    @Override public div mount() {
                        return new div() {{
                          innerText = "clicked " + n + " times";
                          onclick = ev -> effect(n += 1);
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
                    int n = 0;
                   \s
                    @Override
                    public div mount() {
                        return (()->{
                            com.over64.greact.dom.HTMLNativeElements.div _holder0 = null;
                            {
                                (_render0 = ()->{
                                    final com.over64.greact.dom.HTMLNativeElements.div _el0 = com.greact.model.JSExpression.of("document.createElement(\\'div\\')");
                                    _el0.innerText = "clicked " + n + " times";
                                    _el0.onclick = (ev)->_effect0(n += 1);
                                    _holder0 = com.over64.greact.dom.GReact.replace(_el0, _holder0);
                                }).run();
                            }
                            return _holder0;
                        }).call();
                    }
                    private java.lang.Runnable _render0;
                   \s
                    private void _effect0(java.lang.Object x0) {
                        if (_render0 != null) _render0.run();
                    }
                }"""
        );
    }
}
