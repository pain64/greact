package ui.transform;

import com.over64.greact.EffectCallFinder;
import com.over64.greact.NewClassPatcher;
import com.over64.greact.ViewEntryFinder;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import org.junit.jupiter.api.Test;
import util.AnalyzeAssertionsCompiler;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static util.AnalyzeAssertionsCompiler.withAssert;

public class _03EffectsTest {

    static class PatchedLikeAssert extends AnalyzeAssertionsCompiler.CompilerAssertion<String> {
        @Override public void doAssert(Context ctx, JCTree.JCCompilationUnit cu, String cuExpected) {
            var effectMap = new EffectCallFinder(ctx).find(cu);
            new ViewEntryFinder(ctx).find(cu)
                .forEach(ce -> {
                    var effectsForClass = effectMap.getOrDefault(ce.classDecl(), new ArrayList<>());
                    new NewClassPatcher(ctx).patch(ce, effectsForClass);
                });

            assertEquals(cuExpected, cu.toString());
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
                        final com.over64.greact.dom.HTMLNativeElements.div _root = com.over64.greact.dom.GReact.mountMe("div");
                        return com.over64.greact.dom.GReact.entry(()->{
                            (_render0 = ()->{
                                _root.replaceChildren();
                                _root.innerText = "clicked " + n + " times";
                                _root.onclick = (ev)->_effect0(n += 1);
                            }).run();
                        });
                    }
                    private java.lang.Runnable _render0;
                   \s
                    private void _effect0(java.lang.Object x0) {
                        if (_render0 != null) _render0.run();
                    }
                }"""
        );
    }

    String xxx = """
        class A implements Component0<div> {
            int nClicks = 0;
            
            @Override div mount() {
                return new div() {{
                    new h1("number of clicks: " + nClicks);
                    new button() {{
                        onclick = ev -> effect(nClicks += 1);
                    }};
                }}
            }
        }
        """;

    String yyy = """
        class A implements Component0<div> {
            int nClicks = 0;
            Runnable _view0;
            
            void _effect0(Object x0, Object x1) {
                _view0.run();
            }
               
            @Override div mount() {
                final _root = GReact.element;
                return GReact.entry(() -> {
                    GReact.make(_root, "h1", (_el0) -> {
                        (_view0 = () -> {
                            _el0.replaceChildren();
                            innerText = "number of clicks: " + nClicks;
                        }).run();
                    });
                    GReact.make(_root, "button", (_el1) -> {
                        _el1.onclick = ev -> _effect0(nClicks += 1);
                    });
                });
            }
        }
        """;

    String repl = """
        this._el0 = document.createElement('h1');
        (this.view0 = async (_re) -> {
            let __el0 = _re ? document.createElement('h1') : this._el0;
            
            __el0.innerText = "hello, GReact";
            
            if(_re) {
                _el0.parentNode.replaceChild(__el0, _el0);
                this._el0 = __el0;
            }
        }).run(false);
        """;
}
