package com.over64.greact;

import com.over64.Meta;
import com.over64.QueryBuilder;
import com.over64.TypesafeSql;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
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
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class TypesafeSqlChecker implements AutoCloseable {

    static class SchemaCheckException extends RuntimeException {
        final JCTree tree;
        final String message;

        public SchemaCheckException(JCTree tree, String message) {
            super(message);
            this.message = message;
            this.tree = tree;
        }
    }

    record CheckHandler(int parametersCount, Checker checker) { }
    interface Checker {
        void check(int parametersCount, JCTree.JCMethodInvocation invoke);
    }

    final Symtab symtab;
    final Names names;
    final Types types;
    final Util util;
    final CommandLine cmd;
    final Symbols symbols;
    final HikariDataSource ds;
    final Map<Symbol.MethodSymbol, CheckHandler> checkHandlers = new HashMap<>();

    class Symbols {
        Symbol.ClassSymbol clTsql = util.lookupClass(TypesafeSql.class);
        Symbol.ClassSymbol clConnection = util.lookupClass(Connection.class);

        List<Symbol.MethodSymbol> mtExec = util.lookupMemberAll(clTsql, "exec");
        List<Symbol.MethodSymbol> mtArray = util.lookupMemberAll(clTsql, "array");
        List<Symbol.MethodSymbol> mtUniqueOrNull = util.lookupMemberAll(clTsql, "uniqueOrNull");
        List<Symbol.MethodSymbol> mtSelect = util.lookupMemberAll(clTsql, "select");
        List<Symbol.MethodSymbol> mtSelectOne = util.lookupMemberAll(clTsql, "selectOne");
        List<Symbol.MethodSymbol> mtUpdateSelf = util.lookupMemberAll(clTsql, "updateSelf");
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

        System.out.println("####CREATE DATASOURCE AGAIN");
        this.ds = new HikariDataSource() {{
            setDriverClassName(driverClassName);
            setJdbcUrl(jdbcUrl);
            setUsername(username);
            setPassword(password);
            setMaximumPoolSize(8);
            setConnectionTimeout(10000);
        }};

        BiConsumer<List<Symbol.MethodSymbol>, Checker> add = (mtList, checker) -> {
            for (var mt : mtList)
                checkHandlers.put(mt, new CheckHandler(mt.params().size(), checker));
        };

        add.accept(symbols.mtExec, this::checkExec);
        add.accept(symbols.mtArray, this::checkArray);
        add.accept(symbols.mtUniqueOrNull, this::checkArray);
        add.accept(symbols.mtSelect, this::checkSelect);
        add.accept(symbols.mtSelectOne, this::checkSelect);
        add.accept(symbols.mtInsertSelf, this::checkInsertSelf);
        add.accept(symbols.mtUpdateSelf, this::checkUpdateSelf);
        add.accept(symbols.mtDeleteSelf, this::checkDeleteSelf);
    }

    @Override public void close() throws Exception {
        ds.close();
        executor.shutdownNow();
        var __ = executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
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

    final ThreadPoolExecutor executor = new ThreadPoolExecutor(
        0, 8, 60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>()
    );

    public void apply(JCTree.JCCompilationUnit cu) {
        var futures = new ArrayList<CompletableFuture<Void>>();

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
                if (handler != null) {
                    var future = new CompletableFuture<Void>();
                    futures.add(future);

                    executor.submit(() -> {
                        try {
                            handler.checker.check(handler.parametersCount, tree);
                            future.complete(null);
                        } catch (Exception e) {
                            future.completeExceptionally(e);
                        }
                    });
                }
            }
        });

        for (var future : futures)
            try {
                future.get();
            } catch (ExecutionException e) {
                var cause = e.getCause();
                if (cause instanceof SchemaCheckException)
                    util.writeCompilationError(
                        cu, ((SchemaCheckException) cause).tree,
                        "schema check\n" + ((SchemaCheckException) cause).message
                    );
                else throw new RuntimeException(e);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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

    String extractQuery(JCTree.JCMethodInvocation invoke, int argumentIndex) {
        var queryExpr = invoke.args.get(argumentIndex);

        if (queryExpr instanceof JCTree.JCLiteral literal) {
            var queryString = (String) literal.value;
            if (queryString.isBlank())
                throw new SchemaCheckException(
                    queryExpr, "sql query or query part cannot be empty"
                );

            return (String) literal.value;
        }

        throw new SchemaCheckException(
            queryExpr, "expected query to be literal"
        );
    }

    List<JCTree.JCExpression> extractArguments(JCTree.JCMethodInvocation invoke, int startIndex) {
        return invoke.args.stream().skip(startIndex).toList();
    }

    record QueryInfo(
        ResultSetMetaData rsMetadata, ParameterMetaData paramMetadata
    ) { }

    interface Validator {
        void validate(QueryInfo info) throws SQLException, SchemaCheckException;
    }

    void validateQuery(JCTree.JCMethodInvocation invoke, String query, Validator callback) {
        try (var conn = ds.getConnection()) {
            final ResultSetMetaData rsMetadata;
            final ParameterMetaData paramMetadata;

            try (var pstmt = conn.prepareStatement(query)) {

                rsMetadata = pstmt.getMetaData();
                paramMetadata = pstmt.getParameterMetaData();
                callback.validate(new QueryInfo(rsMetadata, paramMetadata));
            } catch (SQLException ex) {
                throw new SchemaCheckException(
                    invoke, query + "\n    ^^^ " + ex.getMessage()
                );
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean isTypeAssignable(String from, String to) {
        return from.equals(to) || switch (from) {
            case "java.lang.Byte" -> to.equals("byte");
            case "byte" -> to.equals("java.lang.Byte");
            case "java.lang.Short" -> to.equals("short");
            case "short" -> to.equals("java.lang.Short");
            case "java.lang.Boolean" -> to.equals("boolean");
            case "boolean" -> to.equals("java.lang.Boolean");
            case "java.lang.Integer" -> to.equals("int");
            case "int" -> to.equals("java.lang.Integer");
            case "java.lang.Long" -> to.equals("long");
            case "long" -> to.equals("java.lang.Long");
            default -> false;
        };
    }

    // TODO:
    //   2. реализация insertSelf
    //   3. реализация updateSelf
    //   4. реализация deleteSelf
    //   5. уточнение номера поля если это field (важно для insertSelf и т.д)
    //   8. убрать аннотации в пакет
    //   9. смержить с master
    //   10. сделать файл compile_time_sql_checks_enabled,
    //      наличие которого включает проверки sql при сборке
    //   11. переделать Meta с records на классы

    private void checkResultSetTypes(
        JCTree.JCMethodInvocation invoke,
        Meta.ClassMeta<Symbol.ClassSymbol, JCTree.JCMethodDecl> meta,
        String query, QueryInfo info
    ) throws SQLException, SchemaCheckException {

        int parameterCount = info.rsMetadata.getColumnCount();
        if (parameterCount != meta.fields().size())
            throw new SchemaCheckException(
                invoke, """
                %s
                ^^^ column count mismatch
                    expected %s has %s"""
                .formatted(query, parameterCount, meta.fields().size())
            );

        for (int i = 1; i <= info.rsMetadata.getColumnCount(); i++) {
            var from = info.rsMetadata.getColumnClassName(i);
            var to = meta.fields().get(i - 1).info().getReturnType().type.tsym.getQualifiedName().toString();

            if (!isTypeAssignable(from, to))
                throw new SchemaCheckException(
                    invoke, """
                    %s
                    ^^^ result type mismatch for column %d (%s --> %s)
                        expected %s has %s"""
                    .formatted(
                        query, i - 1, info.rsMetadata.getColumnName(i),
                        meta.fields().get(i - 1).name(), from, to
                    )
                );
        }
    }

    private void checkArgumentsTypes(
        JCTree.JCMethodInvocation invoke, String query,
        QueryInfo info, List<JCTree.JCExpression> arguments
    ) throws SQLException {

        int parameterCount = info.paramMetadata.getParameterCount();
        if (parameterCount != arguments.size())
            throw new SchemaCheckException(
                invoke, """
                %s
                ^^^ parameter count mismatch
                    expected %s has %s"""
                .formatted(query, parameterCount, arguments.size())
            );

        for (int i = 1; i <= info.paramMetadata().getParameterCount(); i++) {
            var argument = arguments.get(i - 1);
            var from = info.paramMetadata.getParameterClassName(i);
            var to = argument.type.tsym.getQualifiedName().toString();

            if (!isTypeAssignable(from, to))
                throw new SchemaCheckException(
                    argument, """
                    %s
                    ^^^ type mismatch for parameter %d
                        expected %s has %s"""
                    .formatted(query, i - 1, from, to)
                );
        }
    }

    void checkExec(int parameterCount, JCTree.JCMethodInvocation invoke) {
        var queryParameterIndex = switch (parameterCount) {
            case 3 -> 1;
            case 2 -> 0;
            default -> throw new RuntimeException("unreachable");
        };

        var query = extractQuery(invoke, queryParameterIndex);
        var arguments = extractArguments(invoke, queryParameterIndex + 1);
        var mapped = TypesafeSql.mapQueryArgs(query, arguments.size());

        validateQuery(invoke, mapped.newExpr(), info ->
            checkArgumentsTypes(invoke, query, info, arguments));
    }

    void checkArray(int parameterCount, JCTree.JCMethodInvocation invoke) {
        var queryParameterIndex = switch (parameterCount) {
            case 4 -> 2;
            case 3 -> 1;
            default -> throw new RuntimeException("unreachable");
        };

        var meta = extractMeta(invoke);
        var query = extractQuery(invoke, queryParameterIndex);
        var arguments = extractArguments(invoke, queryParameterIndex + 1);
        var mapped = TypesafeSql.mapQueryArgs(query, arguments.size());

        validateQuery(invoke, mapped.newExpr(), info -> {
            checkArgumentsTypes(invoke, query, info, arguments);
            checkResultSetTypes(invoke, meta, query, info);
        });
    }

    void checkSelect(int parameterCount, JCTree.JCMethodInvocation invoke) {
        var meta = extractMeta(invoke);
        var queryParameterIndex = switch (parameterCount) {
            case 4 -> 2;
            case 3 -> 1;
            case 2, 1 -> -1;
            default -> throw new RuntimeException("unreachable");
        };

        if (queryParameterIndex == -1) {
            var query = QueryBuilder.selectQuery(meta, "");
            validateQuery(invoke, query,
                info -> checkResultSetTypes(invoke, meta, query, info)
            );
        } else {
            var expression = extractQuery(invoke, queryParameterIndex);
            var query = QueryBuilder.selectQuery(meta, expression);
            var arguments = extractArguments(invoke, queryParameterIndex + 1);
            var mapped = TypesafeSql.mapQueryArgs(query, arguments.size());

            validateQuery(invoke, mapped.newExpr(), info -> {
                checkArgumentsTypes(invoke, mapped.newExpr(), info, arguments);
                checkResultSetTypes(invoke, meta, mapped.newExpr(), info);
            });
        }
    }

    void checkInsertSelf(int parameterCount, JCTree.JCMethodInvocation invoke) {
        var meta = extractMeta(invoke);
        var query = QueryBuilder.insertSelfQuery(meta);
        validateQuery(invoke, query,
            info -> checkResultSetTypes(invoke, meta, query, info));
    }

    // in - paramsMetadata - instance field data, where id
    // also check that entity has at least one id
    void checkUpdateSelf(int parameterCount, JCTree.JCMethodInvocation invoke) {
//        var meta = extractMeta(invoke);
//        var query = QueryBuilder.updateSelfQuery(meta);
//        var ps = createPreparedStatement(query);
//        var parameterMetadata = ps.getParameterMetaData();
//
//        int parameterCount = parameterMetadata.getParameterCount();
//        if (parameterCount != meta.fields().size())
//            throw new SchemaCheckException("""
//                %s
//                ^^^ column count mismatch
//                    expected %s has %s
//                """
//                .formatted(query, parameterCount, meta.fields().size())
//            );
//
//        var isId = "";
//        var typeVar = new ArrayList<String>();
//        var typeVar2 = new ArrayList<String>();
//
//        for (int i = 0; i < meta.fields().size(); i++) {
//            typeVar2.add(parameterMetadata.getParameterClassName(i + 1));
//            if (meta.fields().get(i).isId())
//                isId = meta.fields().get(i).info().getReturnType().type.tsym.
//                    getQualifiedName().toString();
//            else
//                typeVar.add(meta.fields().get(i).info().getReturnType().type.tsym.
//                    getQualifiedName().toString());
//
//        }
//
//        typeVar.add(isId);
//
//        for (int i = 1; i <= ((QueryInfo) typeVar2).rsMetadata.getColumnCount(); i++) {
//            var from = ((QueryInfo) typeVar2).rsMetadata.getColumnClassName(i);
//            var to = meta.fields().get(i - 1).info().getReturnType().type.tsym.getQualifiedName().toString();
//
//            if (typeEquals(from, to))
//                throw new SchemaCheckException("""
//                    %s
//                    ^^^ row type mismatch for %s(%s)
//                        expected %s has %s
//                    """
//                    .formatted(
//                        query, meta.fields().get(i).name(), i, from, to
//                    )
//                );
//        }
    }

    // in - paramsMetadata - id of entity
    // also check that entity has at least one id
    void checkDeleteSelf(int parameterCount, JCTree.JCMethodInvocation invoke) {
//        var meta = extractMeta(invoke);
//        var query = QueryBuilder.deleteSelfQuery(meta);
//
//        var ps = createPreparedStatement(query);
//        var parameterMetadata = ps.getParameterMetaData();
//        var varId = Objects.requireNonNull(meta.fields().stream()
//                .filter(Meta.FieldRef::isId)
//                .findFirst()
//                .orElse(null))
//            .info().getReturnType().type.tsym.getQualifiedName().toString();
//
//        QueryInfo info = List.of(varId);
//
//        for (int i = 1; i <= info.rsMetadata.getColumnCount(); i++) {
//            var from = info.rsMetadata.getColumnClassName(i);
//            var to = meta.fields().get(i - 1).info().getReturnType().type.tsym.getQualifiedName().toString();
//
//            if (typeEquals(from, to))
//                throw new SchemaCheckException("""
//                    %s
//                    ^^^ row type mismatch for %s(%s)
//                        expected %s has %s
//                    """
//                    .formatted(
//                        query, meta.fields().get(i).name(), i, from, to
//                    )
//                );
//        }
    }
}
