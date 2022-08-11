package com.over64;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class QueryBuilder {
    static String camelCaseToSnakeCase(String str) {
        var result = new StringBuilder();

        result.append(Character.toLowerCase(str.charAt(0)));

        for (int i = 1; i < str.length(); i++) {
            var ch = str.charAt(i);
            if (Character.isUpperCase(ch)) result.append('_').append(Character.toLowerCase(ch));
            else result.append(ch);

        }

        return result.toString();
    }
    public static <T, V> String selectQuery(Meta.ClassMeta<T, V> meta) {
        var fromTable = meta.table();
        return " select\n\t%s\n from\n\t%s\n\t%s".formatted(
            meta.fields().stream()
                .map(f -> (f.atTable() != null && f.atTable().alias() != null ? f.atTable().alias() + "." : "") + camelCaseToSnakeCase(f.atColumn()))
                .collect(Collectors.joining(",\n\t")),
            fromTable.name() + (fromTable.alias() != null ? " " + fromTable.alias() : ""),
            meta.joins().stream()
                .map(join -> {
                    var kind = join.mode().toString().toLowerCase() + " join";
                    var tableExpr = " " + join.table().name() +
                        (join.table().alias() != null ? " " + join.table().alias() : "");

                    return kind + tableExpr + " on " + join.onExpr();
                }).collect(Collectors.joining("\n\t")));
    }
    public static <T, V> String deleteSelfQuery(Meta.ClassMeta<T, V> meta) {
        var idFields = meta.fields().stream()
            .filter(Meta.FieldRef::isId).toList();
        return "delete from " + meta.table().name() + " where " +
            idFields.stream().map(f -> camelCaseToSnakeCase(f.atColumn()) + " = ?")
                .collect(Collectors.joining(" and "));
    }

    public static <T, V> String insertSelfQuery(Meta.ClassMeta<T, V> meta) {
        var nonJoinedFields = meta.fields().stream()
            .filter(f -> f.atTable() == meta.table()).toList();

        var values = nonJoinedFields.stream()
            .map(Meta.FieldRef::atColumn)
            .map(QueryBuilder::camelCaseToSnakeCase)
            .collect(Collectors.joining(", ", "(", ")"));

        var params = new ArrayList<String>();


        for (var field : nonJoinedFields) {
            if (field.sequence() != null) {
                params.add(field.sequence() + ".nextval");
            } else {
                params.add("?");
            }
        }

        return "insert into " + meta.table().name() + values + " values " +
            params.stream().collect(Collectors.joining(", ", "(", ")"));
    }

    public static <T, V> String updateSelfQuery(Meta.ClassMeta<T, V> meta) {
        var nonJoinedFields = meta.fields().stream()
            .filter(f -> f.atTable() == meta.table()).toList();
        var noIdFields = nonJoinedFields.stream()
            .filter(f -> !f.isId()).toList();
        var idFields = nonJoinedFields.stream()
            .filter(f -> f.isId()).toList();

        return "update " + meta.table().name()
            + " set " +
            noIdFields.stream()
                .map(f -> camelCaseToSnakeCase(f.atColumn()) + " = ?")
                .collect(Collectors.joining(", ")) +
            " where " +
            idFields.stream().map(f -> camelCaseToSnakeCase(f.atColumn()) + " = ?")
                .collect(Collectors.joining(" and "));
    }
}
