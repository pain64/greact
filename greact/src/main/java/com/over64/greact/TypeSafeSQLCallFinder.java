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
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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

    public static <T> Meta.Mapper<JCTree.JCExpression, Symbol.RecordComponent, Symbol.ClassSymbol, JCTree.JCMethodDecl> finderSelfMapper() {
        return new Meta.Mapper<>() {

            @Override public String className(JCTree.JCExpression symbol) {
                return symbol.type.tsym.toString();
            }
            @Override public String fieldName(Symbol.RecordComponent field) {
                return field.toString();
            }
            @Override public Stream<Symbol.RecordComponent> readFields(JCTree.JCExpression symbol) {
                return ((Symbol.ClassSymbol) symbol.type.tsym).getRecordComponents().stream().map(n -> (Symbol.RecordComponent) n);
            }
            @Override
            public <A extends Annotation> @Nullable A classAnnotation(JCTree.JCExpression symbol, Class<A> annotationClass) {
                return symbol.type.tsym.getAnnotation(annotationClass);
            }
            @Override
            public <A extends Annotation> @Nullable A fieldAnnotation(Symbol.RecordComponent field, Class<A> annotationClass) {
                return field.getAnnotation(annotationClass);
            }
            @Override public Symbol.ClassSymbol mapClass(JCTree.JCExpression klass) {
                return ((Symbol.ClassSymbol) klass.type.tsym);
            }
            @Override public JCTree.JCMethodDecl mapField(Symbol.RecordComponent field) {
                return field.accessorMeth;
            }
        };
    }
    public static Throwable preparedStatementError;
    static Thread.UncaughtExceptionHandler exceptionHandler = (th, ex) -> preparedStatementError = ex;
    public static final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0L, TimeUnit.SECONDS,
        new SynchronousQueue(), runnable -> {
        Thread thread = new Thread(runnable);
        thread.setUncaughtExceptionHandler(exceptionHandler);
        return thread;
    });

    public void apply(JCTree.JCCompilationUnit cu) {
        cu.accept(new TreeScanner() {
            @Override
            public void visitApply(JCTree.JCMethodInvocation tree) {
                super.visitApply(tree);

                final Symbol methodSym;
                if (tree.meth instanceof JCTree.JCIdent ident)
                    methodSym = ident.sym;
                else if (tree.meth instanceof JCTree.JCFieldAccess field)
                    methodSym = field.sym;
                else return;

                JCTree.JCExpression tableClass = null;
                Meta.ClassMeta<Symbol.ClassSymbol, JCTree.JCMethodDecl> meta;

                if (symbols.selectMethod.contains(methodSym) ||
                    symbols.selectOneMethod.contains(methodSym)) {

                    for (JCTree.JCExpression arg : tree.args) {
                        var ann = TreeInfo.symbol(arg).owner.getAnnotation(TypesafeSql.Table.class);
                        if (ann != null) {
                            tableClass = arg;
                            break;
                        }
                    }
                    if (tableClass == null) throw new IllegalStateException("Record must be annotated with @Table");
                    meta = Meta.parseClass(tableClass, finderMapper());

                } else if (symbols.deleteSelfMethod.contains(methodSym) ||
                    symbols.insertSelfMethod.contains(methodSym) ||
                    symbols.updateSelfMethod.contains(methodSym)) {

                    for (JCTree.JCExpression arg : tree.args) {
                        if (arg.type.tsym.getAnnotation(TypesafeSql.Table.class) != null) {
                            tableClass = arg;
                            break;
                        }
                    }
                    if (tableClass == null) throw new IllegalStateException("Record must be annotated with @Table");
                    meta = Meta.parseClass(tableClass, finderSelfMapper());
                } else {
                    return;
                }

                String query = "";
                if (symbols.selectMethod.contains(methodSym) || symbols.selectOneMethod.contains(methodSym)) {
                    String finalQuery = TypesafeSql.QueryBuilder.selectQuery(meta);
                    executor.execute(() -> {
                        try {
                            var ps = createPreparedStatement(finalQuery);
                            var preparedStatementMetadata = ps.getMetaData();

                            if (preparedStatementMetadata.getColumnCount() != meta.fields().size())
                                throw new RuntimeException("Не совпадает количество колонок");

                            for (int i = 1; i <= preparedStatementMetadata.getColumnCount(); i++) {
                                if (!preparedStatementMetadata.getColumnClassName(i).equals(meta.fields().get(i - 1).info().getReturnType().type.tsym.getQualifiedName().toString())) {
                                    throw new RuntimeException("Не совпадают типы колонок: " + preparedStatementMetadata.getColumnClassName(i) + " - " + meta.fields().get(i - 1).info().getReturnType().type.tsym.getQualifiedName().toString());
                                }
                            }

                            ps.close();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    return;
                } else if (symbols.deleteSelfMethod.contains(methodSym)) {
                    query = TypesafeSql.QueryBuilder.deleteSelfQuery(meta);
                } else if (symbols.insertSelfMethod.contains(methodSym))
                    query = TypesafeSql.QueryBuilder.insertSelfQuery(meta);
                else if (symbols.updateSelfMethod.contains(methodSym))
                    query = TypesafeSql.QueryBuilder.updateSelfQuery(meta);

                String finalQuery = query;

                executor.execute(() -> {
                    try {
                        var ps = createPreparedStatement(finalQuery);
                        var parameterMetadata = ps.getParameterMetaData();

                        if (symbols.insertSelfMethod.contains(methodSym) || symbols.updateSelfMethod.contains(methodSym)) {
                            if (parameterMetadata.getParameterCount() != meta.fields().size())
                                throw new RuntimeException("Не совпадает количество колонок");

                            if (symbols.insertSelfMethod.contains(methodSym)) {
                                for (int i = 1; i <= parameterMetadata.getParameterCount(); i++) {
                                    if (!parameterMetadata.getParameterClassName(i).equals(meta.fields().get(i - 1).info().getReturnType().type.tsym.getQualifiedName().toString())) {
                                        throw new RuntimeException("Не совпадают типы колонок: " + parameterMetadata.getParameterClassName(i) + " - " + meta.fields().get(i - 1).info().getReturnType().type.tsym.getQualifiedName().toString());
                                    }
                                }
                            } else {
                                if (parameterMetadata.getParameterCount() != meta.fields().size())
                                    throw new RuntimeException("Не совпадает количество колонок");

                                String isId = "";
                                var typeVar = new ArrayList<String>();

                                for (int i = 0; i < meta.fields().size(); i++) {
                                    if (meta.fields().get(i).isId()) {
                                        isId = meta.fields().get(i).info().getReturnType().type.tsym.getQualifiedName().toString();
                                    } else {
                                        typeVar.add(meta.fields().get(i).info().getReturnType().type.tsym.getQualifiedName().toString());
                                    }
                                }

                                typeVar.add(isId);

                                for (int i = 1; i <= parameterMetadata.getParameterCount(); i++) {
                                    if (!typeVar.get(i - 1).equals(parameterMetadata.getParameterClassName(i))) throw new RuntimeException("Не совпадают типы колонок");
                                }

                            }
                        } else {
                            var varId = meta.fields().stream().filter(Meta.FieldRef::isId).findFirst().get().info().getReturnType().type.tsym.getQualifiedName().toString();
                            if (!parameterMetadata.getParameterClassName(1).equals(varId)) throw new RuntimeException("Не совпадают типы колонок");
                        }

                        ps.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
    }
    private PreparedStatement createPreparedStatement(String finalQuery) throws SQLException {
        var ds = new HikariDataSource() {{
            //setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
            setDriverClassName("org.postgresql.Driver");
            setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
            setUsername("postgres");
            setPassword("postgres");
            setMaximumPoolSize(2);
            setConnectionTimeout(1000);
        }};

        return ds.getConnection().prepareStatement(finalQuery);
    }
}
