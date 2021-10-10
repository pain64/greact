package ui.transform;

import com.over64.greact.ViewEntryFinder;
import com.over64.greact.ViewEntryFinder.MountMethodViewHolder;
import com.over64.greact.ViewHolderPatcher;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import org.junit.jupiter.api.Test;
import util.AnalyzeAssertionsCompiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static util.AnalyzeAssertionsCompiler.withAssert;

public class _01PatchViewHolderTest {

    static class PatchedLikeAssert extends AnalyzeAssertionsCompiler.CompilerAssertion<String> {
        @Override public void doAssert(Context ctx, JCTree.JCCompilationUnit cu, String expected) {
            var holder = new ViewEntryFinder(ctx).find(cu)
                .get(0).viewHolders().get(0);
            new ViewHolderPatcher(ctx).patch(holder);

            if (holder instanceof MountMethodViewHolder mmh)
                assertEquals(expected, mmh.owner().toString());
            else if (holder instanceof ViewEntryFinder.LambdaViewHolder lvh)
                assertEquals(expected, lvh.lmb().toString());
        }
    }

    @Test public void patch_mount_method() {
        withAssert(PatchedLikeAssert.class, """
                import com.over64.greact.dom.HTMLNativeElements.*;
                class A implements Component0<div> {
                    @Override public div mount() {
                        return new div();
                    }
                }""",
            """
                                    
                @Override
                public div mount() {
                    final com.over64.greact.dom.HTMLNativeElements.div _root = (com.over64.greact.dom.HTMLNativeElements.div)com.over64.greact.dom.GReact.element;
                    return new div();
                }""");
    }

    @Test public void patch_lambda() {
        withAssert(PatchedLikeAssert.class, """
                import com.over64.greact.dom.HTMLNativeElements.*;
                class A {
                    Component0<div> slot = () -> new div();
                }""",
            """
                ()->{
                    final com.over64.greact.dom.HTMLNativeElements.div _root = (com.over64.greact.dom.HTMLNativeElements.div)com.over64.greact.dom.GReact.element;
                    return new div();
                }""");
    }

    @Test void patch_lambda_with_return() {
        withAssert(PatchedLikeAssert.class, """
                import com.over64.greact.dom.HTMLNativeElements.*;
                class A {
                    Component0<div> slot = () ->  {
                        return new div();
                    };
                }""",
            """
                ()->{
                    final com.over64.greact.dom.HTMLNativeElements.div _root = (com.over64.greact.dom.HTMLNativeElements.div)com.over64.greact.dom.GReact.element;
                    return new div();
                }""");
    }

    /* FIXME: add test for custom component */
}
