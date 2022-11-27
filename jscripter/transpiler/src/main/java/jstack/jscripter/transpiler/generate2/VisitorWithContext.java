package jstack.jscripter.transpiler.generate2;

import jstack.jscripter.transpiler.generate.util.JavaStdShim;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Names;

import javax.lang.model.element.Name;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

abstract class VisitorWithContext extends JCTree.Visitor {
    public Output out;
    public Names names;
    public JavaStdShim stdShim;
    public Types types;
    public Trees trees;
    public JCTree.JCCompilationUnit cu;
    public com.sun.tools.javac.util.Name EQUALS_METHOD_NAME;

    Map<JCTree.JCLambda, Boolean> lambdaAsyncInference = new HashMap<>();

    Stack<JCTree.JCClassDecl> classDefs = new Stack<>();
    Map<Name, List<JCTree.JCMethodDecl>> groups = null;

    void withClass(JCTree.JCClassDecl classDef,
                   Map<Name, List<JCTree.JCMethodDecl>> groups,
                   Runnable action) {
        var oldGroups = this.groups;
        classDefs.push(classDef);
        this.groups = groups;
        action.run();
        this.groups = oldGroups;
        classDefs.pop();
    }

    boolean isAsyncContext = false;
    void withAsyncContext(boolean isAsyncContext, Runnable block) {
        var old = this.isAsyncContext;
        this.isAsyncContext = isAsyncContext;
        block.run();
        this.isAsyncContext = old;
    }

    boolean isStaticMethodCall = false;
    void withStaticMethodCall(boolean isStaticMethodCall, Runnable block) {
        var old = this.isStaticMethodCall;
        this.isStaticMethodCall = isStaticMethodCall;
        block.run();
        this.isStaticMethodCall = old;
    }
}
