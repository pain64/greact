package jstack.jscripter.transpiler.generate2.lookahead;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Names;

import javax.lang.model.element.Name;

public class HasSuperConstructorCall extends TreeScanner {
    final Name superName;
    final Name objectName;
    final Name recordName;
    public boolean hasSuperConstructorCall = false;

    public HasSuperConstructorCall(Names names) {
        superName = names.fromString("super");
        objectName = names.fromString("java.lang.Object");
        recordName = names.fromString("java.lang.Record");
    }

    @Override public void visitApply(JCTree.JCMethodInvocation tree) {
        if (tree.meth instanceof JCTree.JCIdent id &&
            id.name.equals(superName) &&
            !id.sym.owner.flatName().equals(objectName) &&
            !id.sym.owner.flatName().equals(recordName)
        )
            hasSuperConstructorCall = true;
        else super.visitApply(tree);
    }
}
