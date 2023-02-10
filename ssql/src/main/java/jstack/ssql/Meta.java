package jstack.ssql;

import jstack.ssql.schema.*;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;


public class Meta {
    public record TableRef(String name, @Nullable String alias) { }
    public record JoinedTable(JoinMode mode, TableRef table, String onExpr) { }
    public record FieldRef<FI>(FI info, String name, TableRef atTable, String atColumn,
                               boolean isId, @Nullable String sequence) {
    }
    public record ClassMeta<CI, FI>(CI info, TableRef table,
                                    List<JoinedTable> joins, List<FieldRef<FI>> fields) { }

    public interface Mapper<C, F, CI, FI> {
        String className(C symbol);
        String fieldName(F field);
        Stream<F> readFields(C symbol);

        @Nullable <A extends Annotation> A classAnnotation(C symbol, Class<A> annotationClass);
        @Nullable <A extends Annotation> A fieldAnnotation(F field, Class<A> annotationClass);

        CI mapClass(C klass);
        FI mapField(F field);
    }

    static TableRef parseTableRef(String tableName) {
        if (tableName.isEmpty()) throw new
            IllegalStateException("@Table: table name cannot be empty");

        var nameAndAlias = tableName.split(" ");
        if (nameAndAlias.length > 2) throw new
            IllegalStateException("@Table: expected 'tableName [alias]', but has: " + tableName);

        var alias = nameAndAlias.length == 2 ? nameAndAlias[1] : null;

        return new TableRef(nameAndAlias[0], alias);
    }

    static JoinedTable parseJoin(Join join) {
        return new JoinedTable(join.mode(), parseTableRef(join.table()), join.on());
    }

    public static <C, F, CI, FI> ClassMeta<CI, FI> parseClass(C klass, Mapper<C, F, CI, FI> mapper) {
        var className = mapper.className(klass);
        var tableAnn = mapper.classAnnotation(klass, Table.class);
        if (tableAnn == null) throw new IllegalStateException(
            className + ": class or record must be annotated with @Table");

        var tableRef = parseTableRef(tableAnn.value());
        var joinAnn = mapper.classAnnotation(klass, Join.class);
        var joinsAnn = mapper.classAnnotation(klass, Joins.class);

        if (joinAnn != null && joinsAnn != null) throw new IllegalStateException(
            className + ": cannot use @Join and @Joins annotation at same time");

        var joins = joinAnn != null ? new Join[]{joinAnn} :
            (joinsAnn != null ? joinsAnn.value() : new Join[]{});
        var joinTables = Arrays.stream(joins).map(Meta::parseJoin).toList();

        var fields = mapper.readFields(klass)
            .map(f -> {
                var isId = mapper.fieldAnnotation(f, Id.class) != null;
                var seqAnn = mapper.fieldAnnotation(f, Sequence.class);
                var atAnn = mapper.fieldAnnotation(f, At.class);

                var sequence = seqAnn != null ? seqAnn.value() : null;
                var fieldName = mapper.fieldName(f);
                final String atName;
                final TableRef fieldTableRef;

                if (atAnn != null) {
                    if (atAnn.value().isEmpty()) throw new RuntimeException(
                        className + ": @At: info name cannot be empty");
                    atName = atAnn.value().endsWith(".")
                        ? atAnn.value() + fieldName : atAnn.value();
                    fieldTableRef = null;
                } else {
                    atName = mapper.fieldName(f);
                    fieldTableRef = tableRef;
                }

                return new FieldRef<>(mapper.mapField(f), fieldName,
                    fieldTableRef, atName, isId, sequence);
            }).toList();

        return new ClassMeta<>(mapper.mapClass(klass), tableRef, joinTables, fields);
    }
}
