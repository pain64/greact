package jstack.jscripter.transpiler.generate2.lookahead;

import jstack.jscripter.transpiler.generate.util.JavaStdShim;
import jstack.jscripter.transpiler.generate.util.Overloads;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeScanner;

import javax.lang.model.element.TypeElement;

public class HasAsyncCalls extends TreeScanner {
    final JavaStdShim stdShim;
    final Types types;
    public boolean hasAsyncCalls = false;

    public HasAsyncCalls(JavaStdShim stdShim, Types types) {
        this.stdShim = stdShim;
        this.types = types;
    }

    @Override public void visitClassDef(JCTree.JCClassDecl tree) { }
    @Override public void visitLambda(JCTree.JCLambda tree) { }
    @Override public void visitApply(JCTree.JCMethodInvocation call) {
        var methodSym = (Symbol.MethodSymbol) TreeInfo.symbol(call.meth);
        var methodOwnerSym = (Symbol.ClassSymbol) methodSym.owner;
        var shimmedType = stdShim.findShimmedType(methodOwnerSym.type);
        var targetMethod = shimmedType != null
            ? stdShim.findShimmedMethod(shimmedType, methodSym)
            : methodSym;

        var info = shimmedType != null
            ? Overloads.methodInfo(types, (TypeElement) shimmedType.tsym, targetMethod)
            : Overloads.methodInfo(types, (TypeElement) methodOwnerSym.type.tsym, methodSym);

        final boolean isAsync;
        if (call.meth instanceof JCTree.JCFieldAccess fa &&
            fa.selected instanceof JCTree.JCParens parens) {

            if (parens.expr instanceof JCTree.JCLambda lmb) {
                var visitor = new HasAsyncCalls(stdShim, types);
                lmb.body.accept(visitor);
                isAsync = visitor.hasAsyncCalls;
            } else if (parens.expr instanceof JCTree.JCAssign assign &&
                assign.rhs instanceof JCTree.JCLambda lmb) {
                var visitor = new HasAsyncCalls(stdShim, types);
                lmb.body.accept(visitor);
                isAsync = visitor.hasAsyncCalls;
            } else isAsync = info.isAsync();
        } else isAsync = info.isAsync();

        if (isAsync) hasAsyncCalls = true;
    }
}
