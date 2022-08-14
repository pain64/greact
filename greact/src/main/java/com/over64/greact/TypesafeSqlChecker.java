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
import com.sun.tools.javac.util.List;
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
import java.util.HashSet;
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

    static class SchemaCheckException extends Exception {
        public SchemaCheckException(String message) { super(message); }
    }

    public Meta.Mapper<Symbol.ClassSymbol,
        Symbol.RecordComponent,
        Symbol.ClassSymbol,
        JCTree.JCMethodDecl> finderMapper() {
        return new Meta.Mapper<>() {
            @Override public String className(Symbol.ClassSymbol symbol) {
                return symbol.className();
            }
            @Override public String fieldName(Symbol.RecordComponent field) {
                return field.toString();
            }
            @Override
            public Stream<Symbol.RecordComponent> readFields(Symbol.ClassSymbol symbol) {
                return symbol.getRecordComponents().stream().map(n -> (Symbol.RecordComponent) n);
            }
            @Override
            public <A extends Annotation> @Nullable
                A classAnnotation(Symbol.ClassSymbol symbol, Class<A> annotationClass) {
                return symbol.getAnnotation(annotationClass);
            }
            @Override
            public <A extends Annotation> @Nullable
                A fieldAnnotation(Symbol.RecordComponent field, Class<A> annotationClass) {
                return field.getAnnotation(annotationClass);
            }
            @Override public Symbol.ClassSymbol mapClass(Symbol.ClassSymbol klass) {
                return klass;
            }
            @Override public JCTree.JCMethodDecl mapField(Symbol.RecordComponent field) {
                return field.accessorMeth;
            }
        };
    }
    Throwable preparedStatementError;
    Thread.UncaughtExceptionHandler exceptionHandler = (th, ex) -> preparedStatementError = ex;
    final ThreadPoolExecutor executor = new ThreadPoolExecutor(
        0,
        Integer.MAX_VALUE,
        0L,
        TimeUnit.SECONDS,
        new LinkedBlockingDeque<>(), runnable -> {
        var thread = new Thread(runnable);
        thread.setUncaughtExceptionHandler(exceptionHandler);
        return thread;
    });
    Connection conn;


    class Symbols {
        Symbol.ClassSymbol clComponent = util.lookupClass(TypesafeSql.class);
        Symbol.ClassSymbol javaSqlConnection = util.lookupClass(java.sql.Connection.class);
        Symbol.ClassSymbol javaLangString = util.lookupClass(java.lang.String.class);
        HashSet<Symbol.MethodSymbol> selectMethod = new HashSet<>() {{
            addAll(util.lookupMemberAll(clComponent, "select"));
        }};
        HashSet<Symbol.MethodSymbol> selectOneMethod = new HashSet<>() {{
            addAll(util.lookupMemberAll(clComponent, "selectOne"));
        }};
        HashSet<Symbol.MethodSymbol> updateSelfMethod = new HashSet<>() {{
            addAll(util.lookupMemberAll(clComponent, "updateSelf"));
        }};
        HashSet<Symbol.MethodSymbol> deleteSelfMethod = new HashSet<>() {{
            addAll(util.lookupMemberAll(clComponent, "deleteSelf"));
        }};
        HashSet<Symbol.MethodSymbol> insertSelfMethod = new HashSet<>() {{
            addAll(util.lookupMemberAll(clComponent, "insertSelf"));
        }};
        HashSet<Symbol.MethodSymbol> arrayMethod = new HashSet<>() {{
            addAll(util.lookupMemberAll(clComponent, "array"));
        }};
        HashSet<Symbol.MethodSymbol> uniqueOrNullMethod = new HashSet<>() {{
            addAll(util.lookupMemberAll(clComponent, "uniqueOrNull"));
        }};
        HashSet<Symbol.MethodSymbol> execMethod = new HashSet<>() {{
            addAll(util.lookupMemberAll(clComponent, "exec"));
        }};

        HashSet<Symbol.MethodSymbol> allMethods = new HashSet<>() {{
            addAll(selectMethod);
            addAll(selectOneMethod);
            addAll(updateSelfMethod);
            addAll(deleteSelfMethod);
            addAll(insertSelfMethod);
            addAll(arrayMethod);
            addAll(uniqueOrNullMethod);
            addAll(execMethod);
        }};
    }

    final Symbols symbols;
    public void apply(JCTree.JCCompilationUnit cu) {
        if (conn == null) {
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

                final Symbol.MethodSymbol methodSym;
                if (tree.meth instanceof JCTree.JCIdent ident)
                    methodSym = (Symbol.MethodSymbol) ident.sym;
                else if (tree.meth instanceof JCTree.JCFieldAccess field)
                    methodSym = (Symbol.MethodSymbol) field.sym;
                else return;

                if (symbols.allMethods.contains(methodSym))
                    executor.execute(() -> {
                        try {
                            if (symbols.execMethod.contains(methodSym)) {
                                checkExec(tree.args);
                                return;
                            }

                            var tableClass = tree.args.get(0).type.tsym ==
                                symbols.javaSqlConnection
                                || tree.args.get(0).type.tsym == symbols.javaLangString
                                ? tree.args.get(1)
                                : tree.args.get(0);

                            var classSymbol = TreeInfo.symbol(tableClass) == null ?
                                tableClass.getTree().type.asElement().enclClass() :
                                (Symbol.ClassSymbol) TreeInfo.symbol(tableClass).owner;

                            var meta = Meta.parseClass(classSymbol, finderMapper());

                            if (symbols.selectMethod.contains(methodSym) ||
                                symbols.selectOneMethod.contains(methodSym))
                                checkSelect(meta, getStringParameter(tree.args));
                            else if (symbols.deleteSelfMethod.contains(methodSym))
                                checkDelete(meta);
                            else if (symbols.insertSelfMethod.contains(methodSym))
                                checkInsert(meta);
                            else if (symbols.updateSelfMethod.contains(methodSym))
                                checkUpdate(meta);
                            else if (symbols.arrayMethod.contains(methodSym) ||
                                symbols.uniqueOrNullMethod.contains(methodSym))
                                checkArray(meta, tree.args);
                        } catch (SchemaCheckException | SQLException e) {
                            throw new RuntimeException(
                                util.treeSourcePosition(cu, tree.meth) + "\n" + e.getMessage());
                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage());
                        }
                    });
            }
        });
    }
    private String getStringParameter(List<JCTree.JCExpression> args) {
        JCTree.JCExpression stringArg = null;
        for (JCTree.JCExpression arg : args) {
            if (arg.type.tsym == symbols.javaLangString) stringArg = arg;
        }
        if (stringArg instanceof JCTree.JCLiteral)
            return ((JCTree.JCLiteral) stringArg).value.toString();
        return "";
    }
    private void checkUpdate(Meta.ClassMeta<Symbol.ClassSymbol,
        JCTree.JCMethodDecl> meta) throws SQLException, SchemaCheckException {
        var query = QueryBuilder.updateSelfQuery(meta);
        var ps = createPreparedStatement(conn, query);
        var parameterMetadata = ps.getParameterMetaData();

        checkCountColumns(meta, query, parameterMetadata.getParameterCount());

        var isId = "";
        var typeVar = new ArrayList<String>();
        var typeVar2 = new ArrayList<String>();

        for (int i = 0; i < meta.fields().size(); i++) {
            typeVar2.add(parameterMetadata.getParameterClassName(i + 1));
            if (meta.fields().get(i).isId())
                isId = meta.fields().get(i).info().getReturnType().type.tsym.
                    getQualifiedName().toString();
            else
                typeVar.add(meta.fields().get(i).info().getReturnType().type.tsym.
                    getQualifiedName().toString());

        }

        typeVar.add(isId);

        checkParametersTypes(meta, query, typeVar, typeVar2);
    }
    private void checkParametersTypes(Meta.ClassMeta<Symbol.ClassSymbol, JCTree.JCMethodDecl> meta, String query, ArrayList<String> typeVar, ArrayList<String> typeVar2) throws SchemaCheckException {
        for (int i = 0; i < typeVar.size(); i++) {
            if (typeEquals(typeVar.get(i), typeVar2.get(i)))
                throw new SchemaCheckException("""
                    %s
                    %s : %s
                    Не совпадают типы колонок
                    Имеем %s, а ожидали %s
                    Имя колонки: %s
                    Индекс колонки: %s""".formatted(query,
                    meta.info(),
                    meta.table().name(),
                    typeVar.get(i),
                    typeVar2.get(i),
                    meta.fields().get(i).name(),
                    i));
        }
    }
    private void checkCountColumns(Meta.ClassMeta<Symbol.ClassSymbol, JCTree.JCMethodDecl> meta, String query, int parameterCount) throws SchemaCheckException {
        if (parameterCount != meta.fields().size())
            throw new SchemaCheckException("""
                %s
                %s : %s
                Не совпадает количество колонок
                Ожидалось %s, а имеем %s
                """.formatted(query,
                meta.info(),
                meta.table().name(),
                parameterCount,
                meta.fields().size()));
    }
    private void checkInsert(Meta.ClassMeta<Symbol.ClassSymbol,
        JCTree.JCMethodDecl> meta) throws SQLException, SchemaCheckException {
        var query = QueryBuilder.insertSelfQuery(meta);
        var ps = createPreparedStatement(conn, query);
        var parameterMetadata = ps.getParameterMetaData();

        checkCountColumns(meta, query, parameterMetadata.getParameterCount());

        var firstTypes = new ArrayList<String>();
        var secondTypes = new ArrayList<String>();

        for (int i = 1; i <= parameterMetadata.getParameterCount(); i++) {
            firstTypes.add(parameterMetadata.getParameterClassName(i));
            secondTypes.add(meta.fields().get(i - 1).info().getReturnType().type.tsym.getQualifiedName().toString());
        }

        checkParametersTypes(meta, query, firstTypes, secondTypes);

    }
    private void checkDelete(Meta.ClassMeta<Symbol.ClassSymbol,
        JCTree.JCMethodDecl> meta) throws SQLException, SchemaCheckException {
        var query = QueryBuilder.deleteSelfQuery(meta);

        var ps = createPreparedStatement(conn, query);
        var parameterMetadata = ps.getParameterMetaData();
        var varId = Objects.requireNonNull(meta.fields().stream()
                .filter(Meta.FieldRef::isId)
                .findFirst()
                .orElse(null))
            .info().getReturnType().type.tsym.getQualifiedName().toString();

        checkParametersTypes(meta, query, new ArrayList<>() {{
            add(parameterMetadata.getParameterClassName(1));
        }}, new ArrayList<>() {{
            add(varId);
        }});
    }
    private void checkSelect(Meta.ClassMeta<Symbol.ClassSymbol, JCTree.JCMethodDecl> meta,
                             String stringQuery) throws SQLException, SchemaCheckException {
        var finalQuery = QueryBuilder.selectQuery(meta, stringQuery);
        checkTypeAndSize(meta, finalQuery);
    }
    private void checkExec(com.sun.tools.javac.util.List<JCTree.JCExpression> args)
        throws SQLException, SchemaCheckException {
        var arg = args.get(0).type.tsym == symbols.javaLangString ? args.get(0) : args.get(1);
        if (arg instanceof JCTree.JCLiteral) {
            var ps = createPreparedStatement(conn, ((JCTree.JCLiteral) arg).value.toString());
            ps.getParameterMetaData();
            ps.getMetaData();
        } else {
            throw new SchemaCheckException(arg + "\nExcepted JCLiteral");
        }
    }
    private void checkArray(Meta.ClassMeta<Symbol.ClassSymbol, JCTree.JCMethodDecl> meta,
                            com.sun.tools.javac.util.List<JCTree.JCExpression> args)
        throws SQLException, SchemaCheckException {
        var query = "";
        for (JCTree.JCExpression arg : args) {
            if (arg.type.tsym == symbols.javaLangString) {
                query = arg.toString();
                break;
            }
        }
        checkTypeAndSize(meta, query.substring(1, query.length() - 1));
    }
    private boolean typeEquals(String firstType, String secondType) {
        if ((firstType.equals("java.lang.Integer") ||
            firstType.equals("int")) &&
            ((secondType.equals("int") ||
                secondType.equals("long") ||
                secondType.equals("java.lang.Integer"))))
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

        conn = ds.getConnection();
    }
    private HikariDataSource getDataSource(String finalUsername,
                                           String finalPassword,
                                           String finalDriverClassName,
                                           String jdbcUrl) {
        return new HikariDataSource() {{
            setDriverClassName(finalDriverClassName);
            setJdbcUrl(jdbcUrl);
            setUsername(finalUsername);
            setPassword(finalPassword);
            setMaximumPoolSize(2);
            setConnectionTimeout(10000);
        }};
    }
    private PreparedStatement createPreparedStatement(Connection conn, String finalQuery) {
        try {
            return conn.prepareStatement(finalQuery);
        } catch (Exception e) {
            throw new RuntimeException(finalQuery);
        }
    }
    private void checkTypeAndSize(Meta.ClassMeta<Symbol.ClassSymbol, JCTree.JCMethodDecl> meta,
                                  String query) throws SQLException, SchemaCheckException {
        var ps = createPreparedStatement(conn, query);
        var preparedStatementMetadata = ps.getMetaData();

        var firstTypes = new ArrayList<String>();
        var secondTypes = new ArrayList<String>();

        checkCountColumns(meta, query, preparedStatementMetadata.getColumnCount());

        for (int i = 1; i <= preparedStatementMetadata.getColumnCount(); i++) {
            firstTypes.add(preparedStatementMetadata.getColumnClassName(i));
            secondTypes.add(meta.fields().get(i - 1).info().getReturnType().type.tsym.getQualifiedName().toString());
        }

        checkParametersTypes(meta, query, firstTypes, secondTypes);
    }
}
