package jstack.greact;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import jstack.greact.SafeSqlChecksRunner.CheckTask;
import jstack.ssql.QueryBuilder;
import jstack.ssql.QueryBuilder.FieldResult;
import jstack.ssql.QueryBuilder.PositionalArgument;
import jstack.ssql.SafeSql;
import jstack.ssql.dialect.Bindings;
import org.apache.commons.cli.CommandLine;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.StandardLocation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

public class SafeSqlPlugin {
    public @interface Depends {
        Class<?> value();
    }

    interface Checker {
        void check(Connection connection, JCTree.JCMethodInvocation invoke);
    }

    final JavacProcessingEnvironment env;
    final Symtab symtab;
    final Names names;
    final Types types;
    final Util util;
    final JavacElements javacElements;
    final Trees trees;
    final CommandLine cmd;
    final Symbols symbols;
    final SafeSqlChecksRunner checksRunner;
    final Map<Symbol.MethodSymbol, Checker> checkHandlers = new HashMap<>();
    final Map<String, String> sqlToJava = new HashMap<>();

    class Symbols {
        Symbol.ClassSymbol clSsql = util.lookupClass(SafeSql.class);
        Symbol.ClassSymbol clClass = util.lookupClass(Class.class);
        List<Symbol.MethodSymbol> mtExec = util.lookupMemberAll(clSsql, "exec");
        List<Symbol.MethodSymbol> mtQuery = util.lookupMemberAll(clSsql, "query");
        List<Symbol.MethodSymbol> mtQueryAsList = util.lookupMemberAll(clSsql, "queryAsList");
        List<Symbol.MethodSymbol> mtQueryAsStream = util.lookupMemberAll(clSsql, "queryAsStream");
        List<Symbol.MethodSymbol> mtQueryOne = util.lookupMemberAll(clSsql, "queryOne");
        List<Symbol.MethodSymbol> mtQueryOneOrNull = util.lookupMemberAll(clSsql, "queryOneOrNull");
    }

    static class SchemaCheckException extends GReactCompileException {
        public SchemaCheckException(JCTree tree, String message) {
            super(tree, message);
        }
    }

    public SafeSqlPlugin(Context context, CommandLine cmd, SafeSqlChecksRunner checkRunner) {
        this.env = JavacProcessingEnvironment.instance(context);
        this.symtab = Symtab.instance(context);
        this.names = Names.instance(context);
        this.types = Types.instance(context);
        this.util = new Util(context);
        this.javacElements = JavacElements.instance(context);
        this.trees = Trees.instance(env);
        this.symbols = new Symbols();
        this.cmd = cmd;
        this.checksRunner = checkRunner;

        Function<String, String> requireOption = name -> {
            if (!cmd.hasOption(name)) throw
                new RuntimeException("expected option " + name + " to be set");
            return cmd.getOptionValue(name);
        };

        var dialect = requireOption.apply("tsql-check-dialect-classname");

        for (var bind : util.lookupClass(dialect).getAnnotation(Bindings.class).value()) {
            String className;
            try {
                className = bind.klass().getName();
            } catch (MirroredTypeException e) {
                className = e.getTypeMirror().toString();
            }

            sqlToJava.put(bind.sqlType(), className);
        }

        BiConsumer<List<Symbol.MethodSymbol>, Checker> add = (mtList, checker) -> {
            for (var mt : mtList) checkHandlers.put(mt, checker);
        };

        add.accept(symbols.mtExec, this::checkExec);
        add.accept(symbols.mtQuery, this::checkQuery);
        add.accept(symbols.mtQueryAsList, this::checkQuery);
        add.accept(symbols.mtQueryAsStream, this::checkQuery);
        add.accept(symbols.mtQueryOne, this::checkQuery);
        add.accept(symbols.mtQueryOneOrNull, this::checkQuery);
    }

    public void apply(JCTree.JCCompilationUnit cu) {
        var tasks = new ArrayList<CheckTask<Void>>();
        cu.accept(new TreeScanner() {
            @Override public void visitClassDef(JCTree.JCClassDecl tree) {
                if (tree.sym.isRecord())
                    for (var rc : tree.sym.getRecordComponents())
                        rc.type = rc.accessor.getReturnType();

                super.visitClassDef(tree);
            }
            @Override public void visitApply(JCTree.JCMethodInvocation tree) {
                super.visitApply(tree);

                final Symbol.MethodSymbol methodSym;
                if (tree.meth instanceof JCTree.JCIdent ident)
                    methodSym = (Symbol.MethodSymbol) ident.sym;
                else if (tree.meth instanceof JCTree.JCFieldAccess field)
                    methodSym = (Symbol.MethodSymbol) field.sym;
                else return;
                var handler = checkHandlers.get(methodSym);

                if (handler != null) tasks.add(conn -> {
                    handler.check(conn, tree);
                    return null;
                });
            }
        });

        try {
            checksRunner.run(tasks);
        } catch (SchemaCheckException e) {
            util.compilationError(cu, e);
        }
    }

    Symbol.ClassSymbol extractToClass(JCTree.JCMethodInvocation invoke) {
        var tableClass = invoke.args.get(1);

        if (tableClass instanceof JCTree.JCFieldAccess && tableClass.type.tsym == symbols.clClass)
            return (Symbol.ClassSymbol) ((JCTree.JCFieldAccess) tableClass).selected.type.tsym;
        else
            return (Symbol.ClassSymbol) tableClass.type.tsym;
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

    void validateQuery(
        Connection conn, JCTree.JCMethodInvocation invoke, String query, Validator callback
    ) {
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
    }

    private boolean isTypeAssignable(String from, String to) {
        if (from.equals(to)) return true;

        return switch (from) {
            case "boolean" -> to.equals("java.lang.Boolean");
            case "char" -> to.equals("java.lang.Char");
            case "byte" -> to.equals("java.lang.Byte");
            case "short" -> to.equals("java.lang.Short");
            case "int" -> to.equals("java.lang.Integer");
            case "long" -> to.equals("java.lang.Long");
            case "java.lang.Boolean" -> to.equals("boolean");
            case "java.lang.Char" -> to.equals("char");
            case "java.lang.Byte" -> to.equals("byte");
            case "java.lang.Short" -> to.equals("short");
            case "java.lang.Integer" -> to.equals("int");
            case "java.lang.Long" -> to.equals("long");
            default -> false;
        };
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
            var to = sqlToJava.getOrDefault(
                info.paramMetadata.getParameterTypeName(argument.sqlParameterNumber),
                info.paramMetadata.getParameterClassName(argument.sqlParameterNumber)
            );
            var from = javaArgument.type.tsym.getQualifiedName().toString();

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

        var from = sqlToJava.getOrDefault(
            info.rsMetadata.getColumnTypeName(1),
            info.rsMetadata.getColumnClassName(1)
        );

        if (!isTypeAssignable(from, toClass.getQualifiedName().toString()))
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
            var from = sqlToJava.getOrDefault(
                info.rsMetadata.getColumnTypeName(fResult.sqlColumnNumber),
                info.rsMetadata.getColumnClassName(fResult.sqlColumnNumber)
            );
            var to = fResult.field.accessor.getReturnType()
                .tsym.getQualifiedName().toString();

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

    void checkExec(Connection connection, JCTree.JCMethodInvocation invoke) {
        var toClass = extractToClass(invoke);
        var javaArguments = extractArguments(invoke, 2);
        var query = QueryBuilder.forExec(
            extractQuery(invoke, 0), javaArguments.size()
        );

        validateQuery(connection, invoke, query.text, info ->
            checkPositionalArguments(invoke, query.text, info, query.arguments, javaArguments));
    }

    void checkQuery(Connection connection, JCTree.JCMethodInvocation invoke) {
        var toClass = extractToClass(invoke);
        var expression = extractQuery(invoke, 0);
        var javaArguments = extractArguments(invoke, 2);
        if (!toClass.isRecord()) {
            var query = QueryBuilder.forQueryScalar(toClass, expression, javaArguments.size());
            validateQuery(connection, invoke, query.text, info -> {
                checkPositionalArguments(invoke, query.text, info, query.arguments, javaArguments);
                checkResultSetScalar(invoke, toClass, expression, info);
            });
        } else {
            var query = QueryBuilder.forQueryTuple(
                ((List<Symbol.RecordComponent>) toClass.getRecordComponents()).stream().toList(),
                expression, javaArguments.size()
            );
            validateQuery(connection, invoke, query.text, info -> {
                checkPositionalArguments(invoke, query.text, info, query.arguments, javaArguments);
                checkResultSetTuple(invoke, query.results, expression, info);
            });
        }
    }

    private static final Set<String> NOT_GENERATE_FOR = Set.of(
        "byte", "Byte",
        "char", "Char",
        "short", "Short",
        "int", "Int",
        "long", "Long",
        "float", "Float",
        "double", "Double",
        "String", "BigDecimal"
    );

    record GeneratedDto(String className, List<String> fields) { }

    static final Pattern SNAKE_TO_CAMEL_CASE_PATTERN = Pattern.compile("_(\\p{L})");

//    String snakeToCamelCase(String snake) {
//        return SNAKE_TO_CAMEL_CASE_PATTERN
//            .matcher(snake).replaceAll(m -> m.group(1).toUpperCase());
//    }

    String sqlNameToJava(String sqlName) {
        var javaName = new StringBuilder();

        Consumer<Character> writeEscaped = ch ->
            javaName.append("$").append((int) ch);

        var firstCh = sqlName.charAt(0);
        if (Character.isJavaIdentifierStart(firstCh)) javaName.append(firstCh);
        else writeEscaped.accept(firstCh);

        for (var i = 1; i < sqlName.length(); i++) {
            var ch = sqlName.charAt(i);

            if (ch == '_' && i + 1 < sqlName.length()) {
                ch = Character.toUpperCase(sqlName.charAt(i + 1));
                i++;
            }

            if (Character.isJavaIdentifierPart(ch)) javaName.append(ch);
            else writeEscaped.accept(ch);
        }

        return javaName.toString();
    }


    GeneratedDto generate(
        Connection connection, String query, int argumentCount, String className
    ) throws SQLException {
        var exprAndArguments = QueryBuilder.mapQueryArgs(query, argumentCount);
        var queryText = exprAndArguments.newExpr();
        var fields = new ArrayList<String>();

        try (var pstmt = connection.prepareStatement(queryText)) {
            var rsMetadata = pstmt.getMetaData();
            for (var i = 1; i <= rsMetadata.getColumnCount(); i++) {
                var fieldName = sqlNameToJava(rsMetadata.getColumnName(i));

                var fieldType = sqlToJava.getOrDefault(
                    rsMetadata.getColumnTypeName(i),
                    rsMetadata.getColumnClassName(i)
                );
                fields.add(
                    "        " + fieldType + " " + fieldName +
                        (i == rsMetadata.getColumnCount() ? "\n" : ",\n")
                );
            }
        }

        return new GeneratedDto(className, fields);
    }

    public boolean generateDto(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        if (set.isEmpty()) return false;

        Set<? extends Element> annotatedElements
            = roundEnv.getElementsAnnotatedWith(set.iterator().next());

        for (var element : annotatedElements) {
            var xxx = (Symbol.ClassSymbol) element;

            var fileExists = true;
            try {
                env.getFiler().getResource(
                    StandardLocation.CLASS_PATH,
                    xxx.packge().getQualifiedName().toString(),
                    xxx.getSimpleName().toString() + "AutoDto.class"
                );
            } catch (FileNotFoundException e) {
                fileExists = false;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (fileExists) continue;

            var queryMethodNames = Set.of(
                names.fromString("query"),
                names.fromString("queryOne"),
                names.fromString("queryOneOrNull")
            );
            var dotClass = names.fromString("class");

            var classTree = (JCTree) trees.getTree(xxx);
            var tasks = new ArrayList<CheckTask<GeneratedDto>>();

            classTree.accept(
                new TreeScanner() {
                    @Override public void visitApply(JCTree.JCMethodInvocation tree) {
                        if (tree.meth instanceof JCTree.JCFieldAccess select &&
                            queryMethodNames.contains(select.name) && tree.args.size() >= 2
                        ) {
                            var queryArgument = tree.args.get(0);
                            var classArgument = tree.args.get(1);

                            if (classArgument instanceof JCTree.JCFieldAccess classExpr
                                && classExpr.name == dotClass
                            ) {
                                var query = ((JCTree.JCLiteral) queryArgument).value.toString();
                                var className = ((JCTree.JCIdent) classExpr.selected).name.toString();

                                if (!NOT_GENERATE_FOR.contains(className))
                                    tasks.add(connection -> {
                                        try {
                                            return generate(
                                                connection, query, tree.args.size() - 2, className
                                            );
                                        } catch (SQLException e) {
                                            throw new SchemaCheckException(
                                                tree, query + "\n    ^^^ " + e.getMessage()
                                            );
                                        }
                                    });
                            }
                        }

                        super.visitApply(tree);
                    }
                });

            var cu = javacElements.getTreeAndTopLevel(xxx, null, null).snd;
            final List<GeneratedDto> generated;

            try {
                generated = checksRunner.run(tasks);
            } catch (SchemaCheckException e) {
                util.compilationError(cu, e);
                return true;
            }

            try {
                var builderFile = env.getFiler()
                    .createSourceFile(xxx.getQualifiedName() + "AutoDto", xxx);

                try (var out = new PrintWriter(builderFile.openWriter())) {
                    out.write("package " +
                        xxx.getQualifiedName().toString()
                            .replace("." + xxx.getSimpleName().toString(), "") +
                        ";\n"
                    );
                    out.write("import jstack.greact.SafeSqlPlugin.Depends;\n\n");
                    out.write("@Depends(%s.class)\n".formatted(xxx.getQualifiedName()));
                    out.write("class %sAutoDto { \n".formatted(xxx.getSimpleName()));
                    for (var gen : generated) {
                        out.write("    public record " + gen.className + "(\n");
                        for (var field : gen.fields) out.write(field);
                        out.write("    ){ }\n");
                    }
                    out.write("}");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }
}
