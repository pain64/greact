package codeview;

import jstack.greact.CodeViewPlugin;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import org.junit.jupiter.api.Test;
import util.AnalyzeAssertionsCompiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static util.AnalyzeAssertionsCompiler.withAssert;

public class CodeViewTest {
    static class PatchedLikeAssert extends AnalyzeAssertionsCompiler.CompilerAssertion<String> {
        @Override
        public void doAssert(Context ctx, JCTree.JCCompilationUnit cu, String cuExpected) {
            new CodeViewPlugin(ctx).apply(cu);
            assertEquals(cuExpected, cu.toString());
        }
    }

    @Test void sampleTest() {
        withAssert(PatchedLikeAssert.class, """
                import jstack.greact.dom.CodeView;
                import jstack.greact.dom.CodeView.CodeAndView;
                import jstack.greact.html.*;
                                  
                public class A implements Component0<div> {
                    Component1<div, CodeAndView> renderer = codeAndView ->
                        new div() {{
                            style.border = "1px red solid";
                            new slot<>(codeAndView.view);
                            new h3(codeAndView.code) {{
                                style.border = "1px green solid";
                            }};
                        }};
                   \s
                    @Override public div mount() {
                        return new div() {{
                            new h1("example1");
                            new CodeView(() -> new h2("hello"), renderer);
                        }};
                    }
                }""",
            """
                                
                import jstack.greact.dom.CodeView;
                import jstack.greact.dom.CodeView.CodeAndView;
                import jstack.greact.html.*;
                                
                public class A implements Component0<div> {
                   \s
                    public A() {
                        super();
                    }
                    Component1<div, CodeAndView> renderer = (codeAndView)->new div(){
                       \s
                        () {
                            super();
                        }
                        {
                            style.border = "1px red solid";
                            new slot<>(codeAndView.view);
                            new h3(codeAndView.code){
                               \s
                                (java.lang.String innerText) {
                                    super(innerText);
                                }
                                {
                                    style.border = "1px green solid";
                                }
                            };
                        }
                    };
                   \s
                    @Override
                    public div mount() {
                        return new div(){
                           \s
                            () {
                                super();
                            }
                            {
                                new h1("example1");
                                new CodeView(()->new h2("hello"), renderer, "new h2(\\"hello\\")");
                            }
                        };
                    }
                }""");
    }
}
