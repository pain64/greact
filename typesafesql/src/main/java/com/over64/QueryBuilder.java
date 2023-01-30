package com.over64;

import com.over64.Meta.ClassMeta;
import com.over64.Meta.FieldRef;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class QueryBuilder {

    @AllArgsConstructor public static class PositionalArgument {
        public int javaArgumentIndex;
        public int sqlParameterNumber;
    }

    @AllArgsConstructor public static class FieldArgument<FI> {
        public FI field;
        public int sqlParameterNumber;
    }

    @AllArgsConstructor public static class FieldResult<FI> {
        public int sqlColumnNumber;
        public FI field;
    }

    @AllArgsConstructor public static class PositionalToVoidQuery {
        public String text;
        public List<PositionalArgument> arguments;
    }

    @AllArgsConstructor public static class PositionalToScalarQuery<CI> {
        public String text;
        public List<PositionalArgument> arguments;
        public CI resultClass;
    }

    @AllArgsConstructor public static class PositionalToTupleQuery<FI> {
        public String text;
        public List<PositionalArgument> arguments;
        public List<FieldResult<FI>> results;
    }

    @AllArgsConstructor public static class TupleToTupleQuery<FI> {
        public String text;
        public List<FieldArgument<FI>> arguments;
        public List<FieldResult<FI>> results;
    }

    static String camelToSnakeCase(String str) {
        var result = new StringBuilder();

        result.append(Character.toLowerCase(str.charAt(0)));

        for (int i = 1; i < str.length(); i++) {
            var ch = str.charAt(i);
            if (Character.isUpperCase(ch)) result.append('_').append(Character.toLowerCase(ch));
            else result.append(ch);

        }

        return result.toString();
    }

    static int nDigits(int n) {
        int length = 0;
        long temp = 1;
        while (temp <= n) {
            length++;
            temp *= 10;
        }
        return length;
    }

    record ArgOffset(int argIdx, int offset) { }
    record ExprAndArguments(String newExpr, List<PositionalArgument> arguments) { }

    static ExprAndArguments mapQueryArgs(String expr, int argumentCount) {
        var offsets = new ArrayList<ArgOffset>();

        for (var i = argumentCount - 1; i >= 0; i--) {
            for (; ; ) {
                var offset = expr.indexOf(":" + (i + 1));
                var iLength = nDigits(i + 1);
                if (offset == -1) break;
                expr = expr.substring(0, offset) +
                    "?" + " ".repeat(iLength) + // NB! keep offsets
                    expr.substring(offset + iLength + 1);
                offsets.add(new ArgOffset(i, offset));
            }
        }

        offsets.sort(Comparator.comparingInt(ArgOffset::offset));

        var arguments = new ArrayList<PositionalArgument>();
        for (var i = 0; i < offsets.size(); i++)
            arguments.add(new PositionalArgument(offsets.get(i).argIdx, i + 1));

        return new ExprAndArguments(expr, arguments);
    }

    public static PositionalToVoidQuery forExec(String text, int argumentCount) {
        var mapped = mapQueryArgs(text, argumentCount);
        return new PositionalToVoidQuery(mapped.newExpr, mapped.arguments);
    }

    public static <CI> PositionalToScalarQuery<CI> forQueryScalar(
        CI toClass, String expr, int argumentCount
    ) {
        var mapped = mapQueryArgs(expr, argumentCount);
        return new PositionalToScalarQuery<>(
            mapped.newExpr, mapped.arguments, toClass
        );
    }

    public static <FI> PositionalToTupleQuery<FI> forQueryTuple(
        List<FI> fields, String expr, int argumentCount
    ) {
        var mapped = mapQueryArgs(expr, argumentCount);
        var results = new ArrayList<FieldResult<FI>>();

        for (var i = 0; i < fields.size(); i++)
            results.add(new FieldResult<>(i + 1, fields.get(i)));

        return new PositionalToTupleQuery<>(
            mapped.newExpr, mapped.arguments, results
        );
    }

    public static <CI, FI> PositionalToTupleQuery<FI> forSelect(
        ClassMeta<CI, FI> meta, String expr, int argumentCount
    ) {
        var fromTable = meta.table();
        var names = new ArrayList<String>();
        var results = new ArrayList<FieldResult<FI>>();

        for (var i = 0; i < meta.fields().size(); i++) {
            var f = meta.fields().get(i);
            var name = (f.atTable() != null && f.atTable().alias() != null
                ? f.atTable().alias() + "." : ""
            ) + camelToSnakeCase(f.atColumn());
            names.add(name);
            results.add(new FieldResult<>(i + 1, f.info()));
        }

        var text = " select\n\t%s\n from\n\t%s\n\t%s".formatted(
            String.join(",\n\t", names),
            fromTable.name() + (fromTable.alias() != null ? " " + fromTable.alias() : ""),
            meta.joins().stream()
                .map(join -> {
                    var kind = join.mode().toString().toLowerCase() + " join";
                    var tableExpr = " " + join.table().name() +
                        (join.table().alias() != null ? " " + join.table().alias() : "");

                    return kind + tableExpr + " on " + join.onExpr();
                }).collect(Collectors.joining("\n\t"))) + "\n" + expr;

        var mapped = mapQueryArgs(text, argumentCount);

        return new PositionalToTupleQuery<>(mapped.newExpr, mapped.arguments, results);
    }

    public static <CI, FI> TupleToTupleQuery<FI> forInsertSelf(ClassMeta<CI, FI> meta) {
        var nonJoinedFields = meta.fields().stream()
            .filter(f -> f.atTable() == meta.table()).toList();

        var values = nonJoinedFields.stream()
            .map(FieldRef::atColumn)
            .map(QueryBuilder::camelToSnakeCase)
            .collect(Collectors.joining(", ", "(", ")"));

        var params = new ArrayList<String>();
        var generated = new ArrayList<String>();
        var arguments = new ArrayList<FieldArgument<FI>>();
        var results = new ArrayList<FieldResult<FI>>();

        for (var fieldRef : nonJoinedFields)
            if (fieldRef.sequence() != null) {
                // FOR ORACLE
                // params.add(fieldRef.sequence() + ".nextval");

                params.add("nextval('" + fieldRef.sequence() + "')");
                results.add(new FieldResult<>(
                    results.size() + 1, fieldRef.info()
                ));
                generated.add(camelToSnakeCase(fieldRef.atColumn()));
            } else {
                params.add("?");
                arguments.add(new FieldArgument<>(fieldRef.info(), arguments.size() + 1));
            }

// FOR ORACLE
//        var text = (generated.isEmpty() ? "" : "begin;\n") +
//            "insert into " + meta.table().name() + values + " values " +
//            params.stream().collect(Collectors.joining(", ", "(", ")")) +
//            (generated.isEmpty()
//                ? ""
//                : " returning " + String.join(", ", generated) + " into " +
//                generated.stream().map(it -> "?").collect(Collectors.joining(", "))
//                + "\nend;"
//            );

        var text =
            "insert into " + meta.table().name() + values + " values " +
                params.stream().collect(Collectors.joining(", ", "(", ")")) +
                (generated.isEmpty()
                    ? ""
                    : " returning " + String.join(", ", generated)
                );

        return new TupleToTupleQuery<>(text, arguments, results);
    }

    static <FI> List<FieldArgument<FI>> fieldsToArguments(List<FieldRef<FI>> fields) {
        var arguments = new ArrayList<FieldArgument<FI>>();
        for (var i = 0; i < fields.size(); i++)
            arguments.add(new FieldArgument<>(fields.get(i).info(), i + 1));
        return arguments;
    }

    public static <CI, FI> TupleToTupleQuery<FI> forUpdateSelf(ClassMeta<CI, FI> meta) {
        var nonJoinedFields = meta.fields().stream()
            .filter(f -> f.atTable() == meta.table()).toList();
        var noIdFields = nonJoinedFields.stream()
            .filter(f -> !f.isId()).toList();
        var idFields = nonJoinedFields.stream()
            .filter(f -> f.isId()).toList();
        var parameterFields = new ArrayList<FieldRef<FI>>() {{
            addAll(noIdFields);
            addAll(idFields);
        }};

        var text = "update " + meta.table().name()
            + " set " +
            noIdFields.stream()
                .map(f -> camelToSnakeCase(f.atColumn()) + " = ?")
                .collect(Collectors.joining(", ")) +
            " where " +
            idFields.stream().map(f -> camelToSnakeCase(f.atColumn()) + " = ?")
                .collect(Collectors.joining(" and "));

        return new TupleToTupleQuery<>(text, fieldsToArguments(parameterFields), List.of());
    }

    public static <CI, FI> TupleToTupleQuery<FI> forDeleteSelf(ClassMeta<CI, FI> meta) {
        var idFields = meta.fields().stream()
            .filter(FieldRef::isId).toList();

        var text = "delete from " + meta.table().name() + " where " +
            idFields.stream().map(f -> camelToSnakeCase(f.atColumn()) + " = ?")
                .collect(Collectors.joining(" and "));

        return new TupleToTupleQuery<>(text, fieldsToArguments(idFields), List.of());
    }
}
