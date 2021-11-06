package com.over64;

import com.over64.TypesafeSql.At;
import com.over64.TypesafeSql.Join;
import com.over64.TypesafeSql.Mode;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.over64.TypesafeSql.Joins;
import static com.over64.TypesafeSql.Table;

class Meta {
    interface FieldFetcher {
        Object fetch(ResultSet rs, int idx) throws SQLException;
    }
    public record TableRef(String name, @Nullable String alias) {}
    public record FieldRef(String name, TableRef atTable, String atColumn, FieldFetcher fetcher,
                           boolean isId, Method accessor, @Nullable String sequence) {}
    public record JoinedTable(Mode mode, TableRef table, String onExpr) {}

    public record ClassMeta<T>(
        Constructor<T> cons,
        TableRef table,
        List<JoinedTable> joins,
        List<FieldRef> fields) {}

    static TableRef parseTableRef(String tableName) {
        if (tableName.isEmpty()) throw new IllegalStateException("@Table: table name cannot be empty");

        var nameAndAlias = tableName.split(" ");
        if (nameAndAlias.length > 2)
            throw new IllegalStateException("@Table: expected: 'tableName [alias]', but has: " + tableName);

        var alias = nameAndAlias.length == 2 ? nameAndAlias[1] : null;

        return new TableRef(nameAndAlias[0], alias);
    }

    static JoinedTable parseJoin(Join join) {
        return new JoinedTable(join.mode(), parseTableRef(join.table()), join.on());
    }

    static FieldFetcher fetcherForFieldType(Class<?> fieldType) {
        if (fieldType == int.class || fieldType == Integer.class) return ResultSet::getInt;
        if (fieldType == long.class || fieldType == Long.class) return ResultSet::getLong;
        return ResultSet::getObject;
    }

    static <T> ClassMeta<T> parseClass(Class<T> klass) {
        var tableAnn = klass.getAnnotation(Table.class);
        if (tableAnn == null)
            throw new IllegalStateException(klass.getName() + ": class o record must be annotated with @Table");
        var tableRef = parseTableRef(tableAnn.value());

        var joinAnn = klass.getAnnotation(Join.class);
        var joinsAnn = klass.getAnnotation(Joins.class);

        if (joinAnn != null && joinsAnn != null)
            throw new IllegalStateException(klass.getSimpleName() + ": cannot use @Join and @Joins annotation on the same time");

        var joins = joinAnn != null ? new Join[]{joinAnn} :
            (joinsAnn != null ? joinsAnn.value() : new Join[]{});
        var joinTables = Arrays.stream(joins).map(Meta::parseJoin).toList();

        var allTables = Stream.concat(
                Stream.of(tableRef), joinTables.stream().map(JoinedTable::table))
            .collect(Collectors.toMap(tr -> tr.alias, tr -> tr));

        var fields = Arrays.stream(klass.getRecordComponents())
            .map(rc -> {
                FieldFetcher fetcher = fetcherForFieldType(rc.getType());
                var isId = rc.getAnnotation(TypesafeSql.Id.class) != null;
                var accessor = rc.getAccessor();
                accessor.setAccessible(true);
                var seqAnn = rc.getAnnotation(TypesafeSql.Sequence.class);
                var sequence = seqAnn != null ? seqAnn.value() : null;
                var fieldName = rc.getName();
                var atAnn = rc.getAnnotation(At.class);
                if (atAnn != null) {
                    var atValue = atAnn.value();
                    if (atValue.isEmpty())
                        throw new RuntimeException(klass.getName() + ": @At: field name cannot be empty");

                    var tableAliasAndCol = atValue.split("\\.");
                    var colName = tableAliasAndCol.length == 2 ? tableAliasAndCol[1] : null;

                    return new FieldRef(fieldName, allTables.get(tableAliasAndCol[0]), colName != null ? colName : fieldName, fetcher, isId, accessor, sequence);
                }

                return new FieldRef(fieldName, tableRef, fieldName, fetcher, isId, accessor, sequence);
            }).toList();

        @SuppressWarnings("unchecked")
        var cons = (Constructor<T>) klass.getDeclaredConstructors()[0];
        cons.setAccessible(true);

        return new ClassMeta<>(cons, tableRef, joinTables, fields);
    }

}
