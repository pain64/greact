package com.over64.greact;

import com.over64.Meta;
import com.over64.TypesafeSql;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

public class TypeSafeSQLCallFinder { ;
    private static Symtab symtab;
    private static Names names;

    public TypeSafeSQLCallFinder(Context context) {
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
    }

    static class Symbols {
        static Symbol.ClassSymbol lookupClass(String name) {
            return symtab.enterClass(symtab.unnamedModule, names.fromString(name));
        }
        static Symbol.ClassSymbol typeSafe = lookupClass(TypesafeSql.class.getName());
    }

    public void apply(JCTree.JCCompilationUnit cu) {
        cu.accept(new TreeScanner() {
            @Override public void visitApply(JCTree.JCMethodInvocation method) {
                super.visitApply(method);
                if (TreeInfo.symbol(method.meth).owner == Symbols.typeSafe) {
                    var methodName = TreeInfo.symbol(method.meth).name;
                    var methodOwner = TreeInfo.symbol(method.meth).owner;
                    var methodArgs = (JCTree) method.args.get(0);

                    System.out.println(methodArgs);
                }
            }
        });

    }
}
