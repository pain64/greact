package jstack.jscripter.transpiler.generate2.lookahead;


import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Names;

import javax.lang.model.element.Name;

public class HasSelfConstructorCall extends TreeScanner {
    final Name thisName;
    public boolean hasSelfConstructorCall = false;

    public HasSelfConstructorCall(Names names) {
        thisName = names.fromString("this");
    }

    @Override public void visitApply(JCTree.JCMethodInvocation tree) {
        if (tree.meth instanceof JCTree.JCIdent id &&
            id.name.equals(thisName))
            hasSelfConstructorCall = true;
        else super.visitApply(tree);
    }
}
