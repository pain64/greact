package jstack.greact;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import com.zaxxer.hikari.HikariDataSource;
import jstack.ssql.Meta;
import jstack.ssql.Meta.ClassMeta;
import jstack.ssql.QueryBuilder;
import jstack.ssql.QueryBuilder.FieldArgument;
import jstack.ssql.QueryBuilder.FieldResult;
import jstack.ssql.QueryBuilder.PositionalArgument;
import jstack.ssql.SafeSql;
import jstack.ssql.schema.Ordinal;
import org.apache.commons.cli.CommandLine;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class SafeSqlChecker implements AutoCloseable {

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
        Symbol.ClassSymbol clSsql = util.lookupClass(SafeSql.class);
        Symbol.ClassSymbol clConnection = util.lookupClass(Connection.class);
        Symbol.ClassSymbol clClass = util.lookupClass(Class.class);
        Symbol.ClassSymbol clString = util.lookupClass(String.class);
        Symbol.ClassSymbol clBoolean = util.lookupClass(Boolean.class);
        Symbol.ClassSymbol clByte = util.lookupClass(Byte.class);
        Symbol.ClassSymbol clShort = util.lookupClass(Short.class);
        Symbol.ClassSymbol clInteger = util.lookupClass(Integer.class);
        Symbol.ClassSymbol clLong = util.lookupClass(Long.class);

        List<Symbol.MethodSymbol> mtExec = util.lookupMemberAll(clSsql, "exec");
        List<Symbol.MethodSymbol> mtQuery = util.lookupMemberAll(clSsql, "query");
        List<Symbol.MethodSymbol> mtQueryOne = util.lookupMemberAll(clSsql, "queryOne");
        List<Symbol.MethodSymbol> mtQueryOneOrNull = util.lookupMemberAll(clSsql, "queryOneOrNull");
        List<Symbol.MethodSymbol> mtSelect = util.lookupMemberAll(clSsql, "select");
        List<Symbol.MethodSymbol> mtSelectOne = util.lookupMemberAll(clSsql, "selectOne");
        List<Symbol.MethodSymbol> mtUpdateSelf = util.lookupMemberAll(clSsql, "updateSelf");
        List<Symbol.MethodSymbol> mtDeleteSelf = util.lookupMemberAll(clSsql, "deleteSelf");
        List<Symbol.MethodSymbol> mtInsertSelf = util.lookupMemberAll(clSsql, "insertSelf");
        List<Symbol.MethodSymbol> mtUpsert = util.lookupMemberAll(clSsql, "upsert");
    }

    public SafeSqlChecker(Context context, CommandLine cmd) {
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
        add.accept(symbols.mtQuery, this::checkQuery);
        add.accept(symbols.mtQueryOne, this::checkQuery);
        add.accept(symbols.mtQueryOneOrNull, this::checkQuery);
        add.accept(symbols.mtSelect, this::checkSelect);
        add.accept(symbols.mtSelectOne, this::checkSelect);
        add.accept(symbols.mtInsertSelf, this::checkInsertSelf);
        add.accept(symbols.mtUpsert, this::checkUpsert);
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
        Symbol.ClassSymbol, Symbol.RecordComponent> finderMapper() {

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

            @Override public Symbol.RecordComponent mapField(Symbol.RecordComponent field) {
                return field;
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

    Symbol.ClassSymbol extractClass(JCTree.JCMethodInvocation invoke) {
        var tableClass = invoke.args.head.type.tsym == symbols.clConnection
            ? invoke.args.get(1)
            : invoke.args.head;

        if (tableClass instanceof JCTree.JCFieldAccess &&
            tableClass.type.tsym == symbols.clClass) {
            System.out.println("####HAS " + (Symbol.ClassSymbol) ((JCTree.JCFieldAccess) tableClass).selected.type.tsym);
            return (Symbol.ClassSymbol) ((JCTree.JCFieldAccess) tableClass).selected.type.tsym;
        } else
            return (Symbol.ClassSymbol) tableClass.type.tsym;
    }

    ClassMeta<Symbol.ClassSymbol, Symbol.RecordComponent>
    extractMeta(JCTree.JCMethodInvocation invoke) {
        return Meta.parseClass(extractClass(invoke), finderMapper());
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

    private boolean isTypeAssignable(Symbol.ClassSymbol from, Symbol.ClassSymbol to) {
        if (from == to) return true;

        if (from.isEnum()) {
            if (from.getAnnotation(Ordinal.class) != null)
                return to == symbols.clInteger || to == symtab.intType.tsym;
            else
                return to == symbols.clString;
        }

        if (to.isEnum()) {
            if (to.getAnnotation(Ordinal.class) != null)
                return from == symbols.clInteger || from == symtab.intType.tsym;
            else
                return from == symbols.clString;
        }

        if (from == symbols.clBoolean) return to == symtab.booleanType.tsym;
        if (from == symtab.booleanType.tsym) return to == symbols.clBoolean;

        if (from == symbols.clByte) return to == symtab.byteType.tsym;
        if (from == symtab.byteType.tsym) return to == symbols.clByte;

        if (from == symbols.clShort) return to == symtab.shortType.tsym;
        if (from == symtab.shortType.tsym) return to == symbols.clShort;

        if (from == symbols.clInteger) return to == symtab.intType.tsym;
        if (from == symtab.intType.tsym) return to == symbols.clInteger;

        if (from == symbols.clLong) return to == symtab.longType.tsym;
        if (from == symtab.longType.tsym) return to == symbols.clLong;

        return false;
    }

    private void checkPositionalArguments(
        JCTree.JCMethodInvocation invoke, String query, QueryInfo info,
        List<PositionalArgument> positionalArguments,
        List<JCTree.JCExpression> javaArguments
    ) throws SQLException {

        int parameterCount = info.paramMetadata.getParameterCount();
        if (parameterCount != positionalArguments.size())
            throw new SchemaCheckException(
                invoke, """
                %s
                ^^^ argument count mismatch
                    expected %s has %s"""
                .formatted(query, parameterCount, positionalArguments.size())
            );

        for (var argument : positionalArguments) {
            var javaArgument = javaArguments.get(argument.javaArgumentIndex);
            var to = util.lookupClass(
                info.paramMetadata.getParameterClassName(argument.sqlParameterNumber)
            );
            var from = (Symbol.ClassSymbol) javaArgument.type.tsym;

            if (!isTypeAssignable(from, to))
                throw new SchemaCheckException(
                    javaArgument, """
                    %s
                    ^^^ type mismatch for java argument %d to sql parameter %d
                        expected %s has %s"""
                    .formatted(
                        query, argument.javaArgumentIndex, argument.sqlParameterNumber, to, from
                    )
                );
        }
    }

    // FIXME: объединить с checkPositionalArguments???
    private void checkFieldArguments(
        JCTree.JCMethodInvocation invoke, String query, QueryInfo info,
        List<FieldArgument<Symbol.RecordComponent>> fieldArguments
    ) throws SQLException {

        int parameterCount = info.paramMetadata.getParameterCount();
        if (parameterCount != fieldArguments.size())
            throw new SchemaCheckException(
                invoke, """
                %s
                ^^^ field argument count mismatch
                    expected %s has %s"""
                .formatted(query, parameterCount, fieldArguments.size())
            );

        for (var argument : fieldArguments) {
            var from = util.lookupClass(
                info.paramMetadata.getParameterClassName(argument.sqlParameterNumber)
            );
            var to = (Symbol.ClassSymbol) argument.field.type.tsym;

            if (!isTypeAssignable(from, to))
                throw new SchemaCheckException(
                    invoke, """
                    %s
                    ^^^ type mismatch for java field `%s` to sql parameter %d
                        expected %s has %s"""
                    .formatted(
                        query, argument.field.name.toString(), argument.sqlParameterNumber, from, to
                    )
                );
        }
    }

    private void checkResultSetScalar(
        JCTree.JCMethodInvocation invoke,
        Symbol.ClassSymbol toClass,
        String query, QueryInfo info
    ) throws SQLException, SchemaCheckException {

        if (info.rsMetadata.getColumnCount() != 1)
            throw new SchemaCheckException(
                invoke, """
                %s
                ^^^ expected 1 column in result set for scalar result"""
                .formatted(query)
            );

        var from = util.lookupClass(info.rsMetadata.getColumnClassName(1));

        if (!isTypeAssignable(from, toClass))
            throw new SchemaCheckException(
                invoke, """
                %s
                ^^^ result type mismatch from sql column %s(1)
                    expected %s has %s"""
                .formatted(
                    query, info.rsMetadata.getColumnName(1), toClass, from
                )
            );
    }

    private void checkResultSetTuple(
        JCTree.JCMethodInvocation invoke,
        List<FieldResult<Symbol.RecordComponent>> resultFields,
        String query, QueryInfo info
    ) throws SQLException, SchemaCheckException {
        if (info.rsMetadata == null)
            if (resultFields.isEmpty()) return;
            else throw new SchemaCheckException(
                invoke, "result set has no output columns, but expected"
            );


        int columnCount = info.rsMetadata.getColumnCount();
        if (columnCount != resultFields.size())
            throw new SchemaCheckException(
                invoke, """
                %s
                ^^^ column count mismatch
                    expected %s has %s"""
                .formatted(query, columnCount, resultFields.size())
            );

        for (var fResult : resultFields) {
            var from = util.lookupClass(
                info.rsMetadata.getColumnClassName(fResult.sqlColumnNumber)
            );
            var to = (Symbol.ClassSymbol) fResult.field.accessorMeth.getReturnType().type.tsym;

            if (!isTypeAssignable(from, to))
                throw new SchemaCheckException(
                    invoke, """
                    %s
                    ^^^ type mismatch for field `%s` from sql column `%s`(%d)
                        expected %s has %s"""
                    .formatted(
                        query, fResult.field.name.toString(),
                        info.rsMetadata.getColumnName(fResult.sqlColumnNumber),
                        fResult.sqlColumnNumber, to, from
                    )
                );
        }
    }

    void checkExec(int parameterCount, JCTree.JCMethodInvocation invoke) {
        var queryParameterIndex = switch (parameterCount) {
            case 3 -> 1;
            case 2 -> 0;
            default -> throw new RuntimeException("unreachable");
        };

        var javaArguments = extractArguments(invoke, queryParameterIndex + 1);
        var query = QueryBuilder.forExec(
            extractQuery(invoke, queryParameterIndex), javaArguments.size()
        );

        validateQuery(invoke, query.text, info ->
            checkPositionalArguments(invoke, query.text, info, query.arguments, javaArguments));
    }

    void checkQuery(int parameterCount, JCTree.JCMethodInvocation invoke) {
        var queryParameterIndex = switch (parameterCount) {
            case 4 -> 2;
            case 3 -> 1;
            default -> throw new RuntimeException("unreachable");
        };

        var toClass = extractClass(invoke);
        var expression = extractQuery(invoke, queryParameterIndex);
        var javaArguments = extractArguments(invoke, queryParameterIndex + 1);
        if (!toClass.isRecord()) {
            var query = QueryBuilder.forQueryScalar(toClass, expression, javaArguments.size());
            validateQuery(invoke, query.text, info -> {
                checkPositionalArguments(invoke, query.text, info, query.arguments, javaArguments);
                checkResultSetScalar(invoke, toClass, expression, info);
            });
        } else {
            var query = QueryBuilder.forQueryTuple(
                ((List<Symbol.RecordComponent>) toClass.getRecordComponents()).stream().toList(),
                expression, javaArguments.size()
            );
            validateQuery(invoke, query.text, info -> {
                checkPositionalArguments(invoke, query.text, info, query.arguments, javaArguments);
                checkResultSetTuple(invoke, query.results, expression, info);
            });
        }
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
            var query = QueryBuilder.forSelect(meta, "", 0);
            validateQuery(invoke, query.text,
                info -> checkResultSetTuple(invoke, query.results, query.text, info)
            );
        } else {
            var expression = extractQuery(invoke, queryParameterIndex);
            var javaArguments = extractArguments(invoke, queryParameterIndex + 1);
            var query = QueryBuilder.forSelect(meta, expression, javaArguments.size());

            validateQuery(invoke, query.text, info -> {
                checkPositionalArguments(invoke, query.text, info, query.arguments, javaArguments);
                checkResultSetTuple(invoke, query.results, query.text, info);
            });
        }
    }

    void checkInsertSelf(int parameterCount, JCTree.JCMethodInvocation invoke) {
        var meta = extractMeta(invoke);
        var query = QueryBuilder.forInsertSelf(meta);
        validateQuery(invoke, query.text, info -> {
            checkFieldArguments(invoke, query.text, info, query.arguments);
            checkResultSetTuple(invoke, query.results, query.text, info);
        });
    }

    void checkUpsert(int parameterCount, JCTree.JCMethodInvocation invoke) {
        var meta = extractMeta(invoke);
        var query = QueryBuilder.forUpsert(meta);
        validateQuery(invoke, query.text, info ->
            checkFieldArguments(invoke, query.text, info, query.arguments)
        );
    }

    void checkUpdateSelf(int parameterCount, JCTree.JCMethodInvocation invoke) {
        var meta = extractMeta(invoke);
        var query = QueryBuilder.forUpdateSelf(meta);
        validateQuery(invoke, query.text, info -> {
            checkFieldArguments(invoke, query.text, info, query.arguments);
            checkResultSetTuple(invoke, query.results, query.text, info);
        });
    }

    void checkDeleteSelf(int parameterCount, JCTree.JCMethodInvocation invoke) {
        var meta = extractMeta(invoke);
        var query = QueryBuilder.forDeleteSelf(meta);
        validateQuery(invoke, query.text, info -> {
            checkFieldArguments(invoke, query.text, info, query.arguments);
            checkResultSetTuple(invoke, query.results, query.text, info);
        });
    }
}
