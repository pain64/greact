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
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.sql.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class TypesafeSqlChecker {
    static class SchemaCheckException extends RuntimeException {
        public SchemaCheckException(String message) { super(message); }
    }

    interface CheckHandler {
        void check(JCTree.JCMethodInvocation invoke);
    }

    final Symtab symtab;
    final Names names;
    final Types types;
    final Util util;
    final CommandLine cmd;
    final Symbols symbols;
    final DataSource ds;
    final Map<Symbol.MethodSymbol, CheckHandler> checkHandlers;

    class Symbols {
        Symbol.ClassSymbol clTsql = util.lookupClass(TypesafeSql.class);
        Symbol.ClassSymbol clConnection = util.lookupClass(Connection.class);

        List<Symbol.MethodSymbol> mtExec = util.lookupMemberAll(clTsql, "exec");
        List<Symbol.MethodSymbol> mtArray = util.lookupMemberAll(clTsql, "array");
        List<Symbol.MethodSymbol> mtUniqueOrNull = util.lookupMemberAll(clTsql, "uniqueOrNull");
        List<Symbol.MethodSymbol> mtSelect = util.lookupMemberAll(clTsql, "select");
        List<Symbol.MethodSymbol> mtSelectOne = util.lookupMemberAll(clTsql, "selectOne");
        List<Symbol.MethodSymbol> mtUpdateSelf = (util.lookupMemberAll(clTsql, "updateSelf");
        List<Symbol.MethodSymbol> mtDeleteSelf = util.lookupMemberAll(clTsql, "deleteSelf");
        List<Symbol.MethodSymbol> mtInsertSelf = util.lookupMemberAll(clTsql, "insertSelf");
    }

    public TypesafeSqlChecker(Context context, CommandLine cmd) {
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
        this.types = Types.instance(context);
        this.util = new Util(context);
        this.symbols = new Symbols();
        this.cmd = cmd;

        Function<String, String> requireOption = name -> {
            if (!cmd.hasOption(name)) throw
                new RuntimeException("expected option " + name + " to be set");
            return cmd.getOptionValue(name);
        };

        var jdbcUrl = requireOption.apply("tsql-check-schema-url");
        var username = requireOption.apply("tsql-check-schema-username");
        var password = requireOption.apply("tsql-check-schema-password");
        var driverClassName = requireOption.apply("tsql-check-driver-classname");

        this.ds = new HikariDataSource() {{
            setDriverClassName(driverClassName);
            setJdbcUrl(jdbcUrl);
            setUsername(username);
            setPassword(password);
            setMaximumPoolSize(2);
            setConnectionTimeout(10000);
        }};

        var self = this;
        checkHandlers = new HashMap<>() {{
            symbols.mtExec.forEach(m -> put(m, self::checkExec));
            symbols.mtArray.forEach(m -> put(m, self::checkArray));
            symbols.mtUniqueOrNull.forEach(m -> put(m, self::checkArray));
            symbols.mtSelect.forEach(m -> put(m, self::checkSelect));
            symbols.mtSelectOne.forEach(m -> put(m, self::checkSelect));
            symbols.mtInsertSelf.forEach(m -> put(m, self::checkInsertSelf));
            symbols.mtUpdateSelf.forEach(m -> put(m, self::checkUpdateSelf));
            symbols.mtDeleteSelf.forEach(m -> put(m, self::checkDeleteSelf));
        }};
    }

    Meta.Mapper<
        Symbol.ClassSymbol, Symbol.RecordComponent,
        Symbol.ClassSymbol, JCTree.JCMethodDecl> finderMapper() {

        return new Meta.Mapper<>() {
            @Override public String className(Symbol.ClassSymbol symbol) {
                return symbol.className();
            }

            @Override public String fieldName(Symbol.RecordComponent field) {
                return field.toString();
            }

            @Override public Stream<Symbol.RecordComponent> readFields(Symbol.ClassSymbol symbol) {
                return symbol.getRecordComponents().stream().map(n -> (Symbol.RecordComponent) n);
            }

            @Override @Nullable public <A extends Annotation> A classAnnotation(
                Symbol.ClassSymbol symbol, Class<A> annotationClass
            ) {
                return symbol.getAnnotation(annotationClass);
            }

            @Override @Nullable public <A extends Annotation> A fieldAnnotation(
                Symbol.RecordComponent field, Class<A> annotationClass
            ) {
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
        0, Integer.MAX_VALUE, 0L, TimeUnit.SECONDS,
        new LinkedBlockingDeque<>(),
        runnable -> {
            var thread = new Thread(runnable);
            thread.setUncaughtExceptionHandler(exceptionHandler);
            return thread;
        });

    public void apply(JCTree.JCCompilationUnit cu) {
        cu.accept(new TreeScanner() {
            @Override public void visitApply(JCTree.JCMethodInvocation tree) {
                super.visitApply(tree);

                final Symbol.MethodSymbol methodSym;
                if (tree.meth instanceof JCTree.JCIdent ident)
                    methodSym = (Symbol.MethodSymbol) ident.sym;
                else if (tree.meth instanceof JCTree.JCFieldAccess field)
                    methodSym = (Symbol.MethodSymbol) field.sym;
                else return;

                var handler = checkHandlers.get(methodSym);
                if (handler != null) handler.check(tree);
            }
        });
    }

    private void checkParametersTypes(Meta.ClassMeta<Symbol.ClassSymbol, JCTree.JCMethodDecl> meta,
                                      String query, ArrayList<String> typeVar,
                                      ArrayList<String> typeVar2) {
        for (int i = 0; i < typeVar.size(); i++)
            if (typeEquals(typeVar.get(i), typeVar2.get(i)))
                throw new SchemaCheckException("""
                    %s
                    %s : %s
                    Не совпадают типы колонок
                    Имеем %s, а ожидали %s
                    Имя колонки: %s
                    Индекс колонки: %s"""
                    .formatted(query,
                        meta.info(), meta.table().name(),
                        typeVar.get(i), typeVar2.get(i),
                        meta.fields().get(i).name(), i
                    )
                );
    }

    private void checkColumnsCount(Meta.ClassMeta<Symbol.ClassSymbol, JCTree.JCMethodDecl> meta,
                                   String query, int parameterCount) {
        if (parameterCount != meta.fields().size())
            throw new SchemaCheckException("""
                %s
                %s : %s
                Не совпадает количество колонок
                Ожидалось %s, а имеем %s
                """
                .formatted(query,
                    meta.info(), meta.table().name(),
                    parameterCount, meta.fields().size())
            );
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

    private void checkTypeAndSize(Meta.ClassMeta<Symbol.ClassSymbol, JCTree.JCMethodDecl> meta,
                                  String query) throws SQLException, SchemaCheckException {
        var ps = createPreparedStatement(query);
        var preparedStatementMetadata = ps.getMetaData();

        var firstTypes = new ArrayList<String>();
        var secondTypes = new ArrayList<String>();

        checkColumnsCount(meta, query, preparedStatementMetadata.getColumnCount());

        for (int i = 1; i <= preparedStatementMetadata.getColumnCount(); i++) {
            firstTypes.add(preparedStatementMetadata.getColumnClassName(i));
            secondTypes.add(meta.fields().get(i - 1).info().getReturnType().type.tsym.getQualifiedName().toString());
        }

        checkParametersTypes(meta, query, firstTypes, secondTypes);
    }

    Meta.ClassMeta<Symbol.ClassSymbol, JCTree.JCMethodDecl>
    extractMeta(JCTree.JCMethodInvocation invoke) {
        var tableClass = invoke.args.head.type.tsym == symbols.clConnection
            ? invoke.args.get(1)
            : invoke.args.head;

        // FIXME: wtf???
        var classSymbol = TreeInfo.symbol(tableClass) == null ?
            tableClass.getTree().type.asElement().enclClass() :
            (Symbol.ClassSymbol) TreeInfo.symbol(tableClass).owner;

        return Meta.parseClass(classSymbol, finderMapper());
    }

    String extractQuery(JCTree.JCMethodInvocation invoke,
                        int idxIfConnectionFirst, int idxIfConnectionNonFirst) {
        var queryExpr = invoke.args.head.type.tsym == symbols.clConnection
            ? invoke.args.get(idxIfConnectionFirst)
            : invoke.args.get(idxIfConnectionNonFirst);

        if (queryExpr instanceof JCTree.JCLiteral literal)
            return (String) literal.value;
        throw new SchemaCheckException("expected query to be literal");
    }

    Stream<JCTree.JCExpression> extractArgs(
        JCTree.JCMethodInvocation invoke, int argsStartIdx
    ) {
        if (invoke.args.head.type.tsym == symbols.clConnection) argsStartIdx++;
        return invoke.args.stream().skip(argsStartIdx);
    }

    record QueryInfo(
        ResultSetMetaData rsMetadata,
        ParameterMetaData paramMetadata
    ) { }

    void validateQuery(String query, Consumer<QueryInfo> callback) {
        try (var conn = ds.getConnection()) {

            final PreparedStatement pstmt;
            try {
                pstmt = conn.prepareStatement(query);
            } catch (SQLException ex) {
                throw new SchemaCheckException(
                    "has query" + query + "but error" + ex.getMessage());
            }

            final ResultSetMetaData rsMetadata;
            final ParameterMetaData paramMetadata;
            try {
                rsMetadata = pstmt.getMetaData();
                paramMetadata = pstmt.getParameterMetaData();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            } finally {
                pstmt.close();
            }

            callback.accept(new QueryInfo(rsMetadata, paramMetadata));

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    // in - paramsMetadata - args
    void checkExec(JCTree.JCMethodInvocation invoke) {
        var query = extractQuery(invoke, 1, 0);
        validateQuery(query, info -> { });
    }

    // in  - paramsMetadata - args
    // out - rsMetadata     - classMeta
    void checkArray(JCTree.JCMethodInvocation invoke) {
        var meta = extractMeta(invoke);
        var query = extractQuery(invoke, 2, 1);
        var args = extractArgs(invoke, 2);
        validateQuery(query, info ->
            checkTypeAndSize(meta, query, info));
    }

    // in  - paramsMetadata - args
    // out - rsMetadata     - classMeta
    void checkSelect(JCTree.JCMethodInvocation invoke) {
        var meta = extractMeta(invoke);
        var expression = extractQuery(invoke, 2, 1);
        var query = QueryBuilder.selectQuery(meta, expression);
        validateQuery(query, info ->
            checkTypeAndSize(meta, query, info));
    }

    // in  - paramsMetadata - instance fields data types without id
    // out - rsMetadata     - id of entity if exists
    // also check that entity has at least one id
    void checkInsertSelf(JCTree.JCMethodInvocation invoke) {
        var meta = extractMeta(invoke);
        var query = QueryBuilder.insertSelfQuery(meta);
        var ps = createPreparedStatement(query);
        var parameterMetadata = ps.getParameterMetaData();

        checkColumnsCount(meta, query, parameterMetadata.getParameterCount());

        var firstTypes = new ArrayList<String>();
        var secondTypes = new ArrayList<String>();

        for (int i = 1; i <= parameterMetadata.getParameterCount(); i++) {
            firstTypes.add(parameterMetadata.getParameterClassName(i));
            secondTypes.add(meta.fields().get(i - 1).info().getReturnType().type.tsym.getQualifiedName().toString());
        }

        checkParametersTypes(meta, query, firstTypes, secondTypes);
    }

    // in - paramsMetadata - instance field data, where id
    // also check that entity has at least one id
    void checkUpdateSelf(JCTree.JCMethodInvocation invoke) {
        var meta = extractMeta(invoke);
        var query = QueryBuilder.updateSelfQuery(meta);
        var ps = createPreparedStatement(query);
        var parameterMetadata = ps.getParameterMetaData();

        checkColumnsCount(meta, query, parameterMetadata.getParameterCount());

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

    // in - paramsMetadata - id of entity
    // also check that entity has at least one id
    void checkDeleteSelf(JCTree.JCMethodInvocation invoke) {
        var meta = extractMeta(invoke);
        var query = QueryBuilder.deleteSelfQuery(meta);

        var ps = createPreparedStatement(query);
        var parameterMetadata = ps.getParameterMetaData();
        var varId = Objects.requireNonNull(meta.fields().stream()
                .filter(Meta.FieldRef::isId)
                .findFirst()
                .orElse(null))
            .info().getReturnType().type.tsym.getQualifiedName().toString();

        checkParametersTypes(meta, query,
            List.of(parameterMetadata.getParameterClassName(1)),
            List.of(varId));
    }
}
