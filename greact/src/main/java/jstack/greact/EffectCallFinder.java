package jstack.greact;

import jstack.greact.html.Component;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class EffectCallFinder {

    final Symtab symtab;
    final Names names;
    final Types types;
    final Util util;

    public EffectCallFinder(Context context) {
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
        this.types = Types.instance(context);
        this.util = new Util(context);
        this.symbols = new Symbols();
    }

    class Symbols {
        Symbol.ClassSymbol clComponent = util.lookupClass(Component.class);
        Symbol.MethodSymbol effectMethod = util.lookupMember(clComponent, "effect");
    }

    final Symbols symbols;

    static class EffectMisuseException extends GReactCompileException {
        public EffectMisuseException(JCTree tree, String message) {
            super(tree, message);
        }
    }

    public record Effect(
        JCTree.JCMethodInvocation invocation, Set<Symbol.VarSymbol> effected
    ) { }

    public LinkedHashMap<JCTree.JCClassDecl, List<Effect>> find(JCTree.JCCompilationUnit cu) {

        var result = new LinkedHashMap<JCTree.JCClassDecl, List<Effect>>();

        cu.accept(new TreeScanner() {
            JCTree.JCClassDecl currentClass;

            @Override public void visitClassDef(JCTree.JCClassDecl tree) {
                if (tree.sym.isAnonymous())
                    super.visitClassDef(tree);
                else {
                    var oldClass = currentClass;
                    currentClass = tree;
                    result.put(currentClass, new ArrayList<>());
                    super.visitClassDef(tree);
                    currentClass = oldClass;
                }
            }

            @Override
            public void visitApply(JCTree.JCMethodInvocation tree) {
                final Symbol methodSym;
                if (tree.meth instanceof JCTree.JCIdent ident)
                    methodSym = ident.sym;
                else if (tree.meth instanceof JCTree.JCFieldAccess field)
                    methodSym = field.sym;
                else return;

                if (methodSym == symbols.effectMethod) {
                    Function<JCTree.JCExpression, Symbol.VarSymbol> fetchVarSymbol = expr -> {
                        if (expr instanceof JCTree.JCIdent id)
                            return (Symbol.VarSymbol) id.sym;
                        else if (expr instanceof JCTree.JCAssign assign) {
                            if (assign.lhs instanceof JCTree.JCIdent id)
                                return (Symbol.VarSymbol) id.sym;
                        } else if (expr instanceof JCTree.JCAssignOp op)
                            if (op.lhs instanceof JCTree.JCIdent id)
                                return (Symbol.VarSymbol) id.sym;

                        throw util.compilationError(
                            cu, new EffectMisuseException(
                                tree.meth, """
                                for ∀ x is variable expected any of:
                                    effect(x)
                                    effect(x = expression)
                                    effect(x op= expression)"""
                            )
                        );
                    };

                    result.get(currentClass)
                        .add(new Effect(tree, Set.of(fetchVarSymbol.apply(tree.args.get(0)))));
                }
                // assert that is class field ???

                super.visitApply(tree);
            }
        });

        return result;
    }
}
