package com.over64.greact;

import com.over64.Meta;
import com.over64.QueryBuilder;
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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class TypesafeSqlChecker {

    final Symtab symtab;
    final Names names;
    final Types types;
    final Util util;
    final CommandLine cmd;

    public TypesafeSqlChecker(Context context, CommandLine cmd) {
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
        this.types = Types.instance(context);
        this.util = new Util(context);
        this.symbols = new Symbols();
        this.cmd = cmd;
    }

    static class FinderData {
        static Meta.Mapper<JCTree.JCExpression, Symbol.RecordComponent, Symbol.ClassSymbol, JCTree.JCMethodDecl> finderMapper(boolean isClass) {
            return new Meta.Mapper<>() {

                @Override public String className(JCTree.JCExpression symbol) {
                    if (isClass) return TreeInfo.symbol(symbol).owner.toString();
                    else return symbol.type.tsym.toString();
                }
                @Override public String fieldName(Symbol.RecordComponent field) {
                    return field.toString();
                }
                @Override
                public Stream<Symbol.RecordComponent> readFields(JCTree.JCExpression symbol) {
                    if (isClass)
                        return ((Symbol.ClassSymbol) TreeInfo.symbol(symbol).owner).getRecordComponents().stream().map(n -> (Symbol.RecordComponent) n);
                    else
                        return ((Symbol.ClassSymbol) symbol.type.tsym).getRecordComponents().stream().map(n -> (Symbol.RecordComponent) n);
                }
                @Override
                public <A extends Annotation> @Nullable A classAnnotation(JCTree.JCExpression symbol, Class<A> annotationClass) {
                    if (isClass)
                        return TreeInfo.symbol(symbol).owner.getAnnotation(annotationClass);
                    else return symbol.type.tsym.getAnnotation(annotationClass);
                }
                @Override
                public <A extends Annotation> @Nullable A fieldAnnotation(Symbol.RecordComponent field, Class<A> annotationClass) {
                    return field.getAnnotation(annotationClass);
                }
                @Override public Symbol.ClassSymbol mapClass(JCTree.JCExpression klass) {
                    if (isClass) return ((Symbol.ClassSymbol) TreeInfo.symbol(klass).owner);
                    else return ((Symbol.ClassSymbol) klass.type.tsym);
                }
                @Override public JCTree.JCMethodDecl mapField(Symbol.RecordComponent field) {
                    return field.accessorMeth;
                }
            };
        }
        static Throwable preparedStatementError;
        static Thread.UncaughtExceptionHandler exceptionHandler = (th, ex) -> preparedStatementError = ex;
        static final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0L, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(), runnable -> {
            Thread thread = new Thread(runnable);
            thread.setUncaughtExceptionHandler(exceptionHandler);
            return thread;
        });
        static Connection conn;
    }

    class Symbols {
        Symbol.ClassSymbol clComponent = util.lookupClass(TypesafeSql.class);
        List<Symbol.MethodSymbol> selectMethod = util.lookupMemberAll(clComponent, "select");
        List<Symbol.MethodSymbol> selectOneMethod = util.lookupMemberAll(clComponent, "selectOne");
        List<Symbol.MethodSymbol> updateSelfMethod = util.lookupMemberAll(clComponent, "updateSelf");
        List<Symbol.MethodSymbol> deleteSelfMethod = util.lookupMemberAll(clComponent, "deleteSelf");
        List<Symbol.MethodSymbol> insertSelfMethod = util.lookupMemberAll(clComponent, "insertSelf");

        List<Symbol.MethodSymbol> allMethods = new ArrayList<>() {{
            addAll(selectMethod);
            addAll(selectOneMethod);
            addAll(updateSelfMethod);
            addAll(deleteSelfMethod);
            addAll(insertSelfMethod);
        }};
    }

    final Symbols symbols;
    public void apply(JCTree.JCCompilationUnit cu) {
        if (FinderData.conn == null) {
            try {
                initConnection(cmd);
            } catch (SQLException | ParseException e) {
                throw new RuntimeException(e);
            }
        }
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
                if (symbols.allMethods.contains((Symbol.MethodSymbol) methodSym))
                    FinderData.executor.execute(() -> {
                        JCTree.JCExpression tableClass = tree.args.get(0).type.toString().equals("java.sql.Connection") ? tree.args.get(1) : tree.args.get(0);

                        Meta.ClassMeta<Symbol.ClassSymbol, JCTree.JCMethodDecl> meta = Meta.parseClass(tableClass, FinderData.finderMapper(
                            symbols.selectMethod.contains((Symbol.MethodSymbol) methodSym) ||
                                symbols.selectOneMethod.contains((Symbol.MethodSymbol) methodSym)));

                        if (symbols.selectMethod.contains((Symbol.MethodSymbol) methodSym) || symbols.selectOneMethod.contains((Symbol.MethodSymbol) methodSym))
                            selectCheck(meta);
                        else if (symbols.deleteSelfMethod.contains((Symbol.MethodSymbol) methodSym))
                            deleteCheck(meta);
                        else if (symbols.insertSelfMethod.contains((Symbol.MethodSymbol) methodSym))
                            insertCheck(meta);
                        else if (symbols.updateSelfMethod.contains((Symbol.MethodSymbol) methodSym))
                            updateCheck(meta);
                    });
            }
        });
    }
    private void updateCheck(Meta.ClassMeta<Symbol.ClassSymbol, JCTree.JCMethodDecl> meta) {
        try {
            var query = QueryBuilder.updateSelfQuery(meta);
            var ps = createPreparedStatement(FinderData.conn, query);
            var parameterMetadata = ps.getParameterMetaData();
            if (parameterMetadata.getParameterCount() != meta.fields().size())
                throw new RuntimeException("Не совпадает количество колонок");

            if (parameterMetadata.getParameterCount() != meta.fields().size())
                throw new RuntimeException("Не совпадает количество колонок");

            String isId = "";
            var typeVar = new ArrayList<String>();

            for (int i = 0; i < meta.fields().size(); i++) {
                if (meta.fields().get(i).isId())
                    isId = meta.fields().get(i).info().getReturnType().type.tsym.getQualifiedName().toString();
                else
                    typeVar.add(meta.fields().get(i).info().getReturnType().type.tsym.getQualifiedName().toString());

            }

            typeVar.add(isId);

            for (int i = 1; i <= parameterMetadata.getParameterCount(); i++) {
                if (typeEquals(typeVar.get(i - 1), parameterMetadata.getParameterClassName(i)))
                    throw new RuntimeException("Не совпадают типы колонок " + typeVar.get(i - 1) + " : " + parameterMetadata.getParameterClassName(i));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }
    private void insertCheck(Meta.ClassMeta<Symbol.ClassSymbol, JCTree.JCMethodDecl> meta) {
        try {
            var query = QueryBuilder.insertSelfQuery(meta);
            var ps = createPreparedStatement(FinderData.conn, query);
            var parameterMetadata = ps.getParameterMetaData();
            if (parameterMetadata.getParameterCount() != meta.fields().size())
                throw new RuntimeException("Не совпадает количество колонок");

            for (int i = 1; i <= parameterMetadata.getParameterCount(); i++) {
                if (typeEquals(parameterMetadata.getParameterClassName(i), meta.fields().get(i - 1).info().getReturnType().type.tsym.getQualifiedName().toString()))
                    throw new RuntimeException("Не совпадают типы колонок: " + parameterMetadata.getParameterClassName(i) + " - " + meta.fields().get(i - 1).info().getReturnType().type.tsym.getQualifiedName().toString());

            }
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }
    private void deleteCheck(Meta.ClassMeta<Symbol.ClassSymbol, JCTree.JCMethodDecl> meta) {
        try {
            var query = QueryBuilder.deleteSelfQuery(meta);
            PreparedStatement ps = createPreparedStatement(FinderData.conn, query);
            var parameterMetadata = ps.getParameterMetaData();
            var varId = Objects.requireNonNull(meta.fields().stream().filter(Meta.FieldRef::isId).findFirst().orElse(null)).info().getReturnType().type.tsym.getQualifiedName().toString();
            if (typeEquals(parameterMetadata.getParameterClassName(1), varId))
                throw new RuntimeException("Не совпадают типы колонок");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void selectCheck(Meta.ClassMeta<Symbol.ClassSymbol, JCTree.JCMethodDecl> meta) {
        try {
            var finalQuery = QueryBuilder.selectQuery(meta);
            var ps = createPreparedStatement(FinderData.conn, finalQuery);
            var preparedStatementMetadata = ps.getMetaData();

            if (preparedStatementMetadata.getColumnCount() != meta.fields().size())
                throw new RuntimeException("Не совпадает количество колонок");

            for (int i = 1; i <= preparedStatementMetadata.getColumnCount(); i++) {
                if (typeEquals(preparedStatementMetadata.getColumnClassName(i), meta.fields().get(i - 1).info().getReturnType().type.tsym.getQualifiedName().toString()))
                    throw new RuntimeException("Не совпадают типы колонок: " + preparedStatementMetadata.getColumnClassName(i) + " - " + meta.fields().get(i - 1).info().getReturnType().type.tsym.getQualifiedName().toString());

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private boolean typeEquals(String firstType, String secondType) {
        if ((firstType.equals("java.lang.Integer") || firstType.equals("int")) && ((secondType.equals("int") || secondType.equals("long") || secondType.equals("java.lang.Integer"))))
            return false;
        return !firstType.equals(secondType);
    }
    private void initConnection(CommandLine cmd) throws SQLException, ParseException {
        var jdbcUrl = cmd.getOptionValue("tsql-check-schema-url");

        var username = "";
        if (cmd.hasOption("tsql-check-schema-username"))
            username = cmd.getOptionValue("tsql-check-schema-username");

        var password = "";
        if (cmd.hasOption("tsql-check-schema-password"))
            password = cmd.getOptionValue("tsql-check-schema-password");

        var driverClassName = "";
        if (cmd.hasOption("tsql-driver-class-name"))
            driverClassName = cmd.getOptionValue("tsql-driver-class-name");

        var ds = getDataSource(username, password, driverClassName, jdbcUrl);

        FinderData.conn = ds.getConnection();
    }
    private HikariDataSource getDataSource(String finalUsername, String finalPassword, String finalDriverClassName, String jdbcUrl) {
        return new HikariDataSource() {{
            setDriverClassName(finalDriverClassName);
            setJdbcUrl(jdbcUrl);
            setUsername(finalUsername);
            setPassword(finalPassword);
            setMaximumPoolSize(2);
            setConnectionTimeout(10000);
        }};
    }
    private PreparedStatement createPreparedStatement(Connection conn, String finalQuery) throws SQLException {
        return conn.prepareStatement(finalQuery);
    }
}
