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
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.function.Function;
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

    public static <T> Meta.Mapper<JCTree.JCExpression, RecordComponent, Constructor<T>, Object> reflectionMapper() {
        return new Meta.Mapper<>() {

            @Override public String className(JCTree.JCExpression symbol) {
                return null;
            }
            @Override public String fieldName(RecordComponent field) {
                return null;
            }
            @Override public Stream<RecordComponent> readFields(JCTree.JCExpression symbol) {
                return null;
            }
            @Override
            public <A extends Annotation> @Nullable A classAnnotation(JCTree.JCExpression symbol, Class<A> annotationClass) {
                return null;
            }
            @Override
            public <A extends Annotation> @Nullable A fieldAnnotation(RecordComponent field, Class<A> annotationClass) {
                return null;
            }
            @Override public Constructor<T> mapClass(JCTree.JCExpression klass) {
                return null;
            }
            @Override public Object mapField(RecordComponent field) {
                return null;
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

                    TypesafeSql.Table annotation  = null;
                    JCTree.JCExpression tableClass = null;

                    for (JCTree.JCExpression arg : tree.args) {
                        var ann = TreeInfo.symbol(arg).owner.getAnnotation(TypesafeSql.Table.class);
                        if (ann != null) {
                            annotation = ann;
                            tableClass = arg;
                        }
                    }

                    if (annotation == null) throw new RuntimeException();

                    var name = TreeInfo.symbol(tableClass).owner;

                    System.out.println(name);
                    System.out.println(annotation);
                }


                super.visitApply(tree);
            }
        });
    }
}
