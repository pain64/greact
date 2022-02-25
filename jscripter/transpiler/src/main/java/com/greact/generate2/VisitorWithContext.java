package com.greact.generate2;

import com.greact.generate.util.JavaStdShim;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
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
}