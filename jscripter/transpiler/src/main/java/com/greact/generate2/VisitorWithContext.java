package com.greact.generate2;

import com.greact.generate.util.JavaStdShim;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Names;

abstract class VisitorWithContext extends JCTree.Visitor {
    Output out;
    Names names;
    JavaStdShim stdShim;
    Types types;
    JCTree.JCCompilationUnit cu;

    Symbol.TypeSymbol currentType;
    boolean isAsyncContext = false;

    void withType(Symbol.TypeSymbol typeSymbol, Runnable block) {
        var old = currentType;
        currentType = typeSymbol;
        block.run();
        currentType = old;
    }

    void withAsyncContext(Runnable block) {
        var old = isAsyncContext;
        isAsyncContext = true;
        block.run();
        isAsyncContext = old;
    }
}
