package com.over64.greact;

import com.over64.Meta;
import com.over64.TypesafeSql;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Stream;

public class TypeSafeSQLCallFinder {

    final Symtab symtab;
    final Names names;
    final Types types;
    final Util util;

    public TypeSafeSQLCallFinder(Context context) {
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
        this.types = Types.instance(context);
        this.util = new Util(context);
        this.symbols = new Symbols();
    }

    class Symbols {
        Symbol.ClassSymbol clComponent = util.lookupClass(TypesafeSql.class);
        List<Symbol.MethodSymbol> selectMethod = util.lookupMemberAll(clComponent, "select");
        List<Symbol.MethodSymbol> selectOneMethod = util.lookupMemberAll(clComponent, "selectOne");
        List<Symbol.MethodSymbol> updateSelfMethod = util.lookupMemberAll(clComponent, "updateSelf");
        List<Symbol.MethodSymbol> deleteSelfMethod = util.lookupMemberAll(clComponent, "deleteSelf");
        List<Symbol.MethodSymbol> insertSelfMethod = util.lookupMemberAll(clComponent, "insertSelf");
    }

    final Symbols symbols;

    public static <T> Meta.Mapper<JCTree.JCExpression, Symbol.RecordComponent, Symbol.ClassSymbol, JCTree.JCMethodDecl> finderMapper() {
        return new Meta.Mapper<>() {

            @Override public String className(JCTree.JCExpression symbol) {
                return TreeInfo.symbol(symbol).owner.toString();
            }
            @Override public String fieldName(Symbol.RecordComponent field) {
                return field.toString();
            }
            @Override public Stream<Symbol.RecordComponent> readFields(JCTree.JCExpression symbol) {
                return ((Symbol.ClassSymbol) TreeInfo.symbol(symbol).owner).getRecordComponents().stream().map(n -> (Symbol.RecordComponent) n);
            }
            @Override
            public <A extends Annotation> @Nullable A classAnnotation(JCTree.JCExpression symbol, Class<A> annotationClass) {
                return TreeInfo.symbol(symbol).owner.getAnnotation(annotationClass);
            }
            @Override
            public <A extends Annotation> @Nullable A fieldAnnotation(Symbol.RecordComponent field, Class<A> annotationClass) {
                return field.getAnnotation(annotationClass);
            }
            @Override public Symbol.ClassSymbol mapClass(JCTree.JCExpression klass) {
                return ((Symbol.ClassSymbol) TreeInfo.symbol(klass).owner);
            }
            @Override public JCTree.JCMethodDecl mapField(Symbol.RecordComponent field) {
                return field.accessorMeth;
            }
        };
    }

    public void apply(JCTree.JCCompilationUnit cu) {

        cu.accept(new TreeScanner() {
            @Override
            public void visitApply(JCTree.JCMethodInvocation tree) {
                final Symbol methodSym;
                if (tree.meth instanceof JCTree.JCIdent ident)
                    methodSym = ident.sym;
                else if (tree.meth instanceof JCTree.JCFieldAccess field)
                    methodSym = field.sym;
                else return;

                if (symbols.selectMethod.contains(methodSym) ||
                    symbols.selectOneMethod.contains(methodSym) ||
                    symbols.deleteSelfMethod.contains(methodSym) ||
                    symbols.insertSelfMethod.contains(methodSym) ||
                    symbols.updateSelfMethod.contains(methodSym)) {

                    JCTree.JCExpression tableClass = null;

                    for (JCTree.JCExpression arg : tree.args) {
                        var ann = TreeInfo.symbol(arg).owner.getAnnotation(TypesafeSql.Table.class);
                        if (ann != null) {
                            tableClass = arg;
                        }
                    }
                    if (tableClass == null) throw new IllegalStateException("Record must be annotated with @Table");

                    Meta.parseClass(tableClass, finderMapper());
                }


                super.visitApply(tree);
            }
        });
    }
}
