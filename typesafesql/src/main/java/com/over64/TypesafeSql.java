package com.over64;

import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class TypesafeSql {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Table {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Sequence {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Id { }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Joins {
        Join[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Join {
        Mode mode();
        String table();
        String on();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface At {
        String value();
    }

    public enum Mode {INNER, LEFT, RIGHT, FULL}

    public final DataSource ds;

    public TypesafeSql(DataSource ds) {
        this.ds = ds;
    }

    interface FieldFetcher {
        Object fetch(ResultSet rs, int idx) throws SQLException;
    }

    static FieldFetcher fetcherForFieldType(Class<?> fieldType) {
        if (fieldType == int.class || fieldType == Integer.class) return ResultSet::getInt;
        if (fieldType == long.class || fieldType == Long.class) return ResultSet::getLong;
        if (fieldType == boolean.class || fieldType == Boolean.class) return ResultSet::getBoolean;
        if (fieldType == String.class) return ResultSet::getString;
        return ResultSet::getObject;
    }

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

    static String snakeCaseToCamelCase(String str) {
        var result = new StringBuilder(str);

        for (int i = 0; i < result.length(); i++) {
            if (result.charAt(i) == '_') {
                result.deleteCharAt(i);
                result.replace(i, i+1, String.valueOf(Character.toUpperCase(result.charAt(i))));
            }
        }

        return result.toString();
    }

    /* FIXME: may handler throw Exception ??? */
    public <T> T withConnection(Function<Connection, T> handler) {
        try (var conn = ds.getConnection()) {
            return handler.apply(conn);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T> T[] array(Connection conn, Class<T> klass, String stmt, Object... args) {
        var exprAndOffsets = mapQueryArgs(stmt, args);
        var offsets = exprAndOffsets.offsets;

        try {
            System.out.println("### EXEC QUERY:\n" + exprAndOffsets.newExpr);
            var pstmt = conn.prepareStatement(exprAndOffsets.newExpr);

            for (var i = 0; i < offsets.size(); i++)
                pstmt.setObject(i + 1, args[offsets.get(i).argIdx]);

            pstmt.execute();
            var rs = pstmt.getResultSet(); // snake to camel
            var data = new ArrayList<T>();

            if (klass.isRecord()) {
                @SuppressWarnings("unchecked")
                var cons = (Constructor<T>) klass.getDeclaredConstructors()[0];
                cons.setAccessible(true);
                var consArgs = new Object[cons.getParameters().length];

                while (rs.next()) {
                    var fetchArgId = 1;
                    for (var i = 0; i < consArgs.length; i++) {
                        var fetcher = fetcherForFieldType(cons.getParameters()[i].getType());
                        consArgs[i] = fetcher.fetch(rs, fetchArgId++);
                    }

                    data.add(cons.newInstance(consArgs));
                }
            } else
                while (rs.next())
                    data.add((T) rs.getObject(1));

            return data.toArray(n -> (T[]) Array.newInstance(klass, n));
        } catch (SQLException | IllegalAccessException | InvocationTargetException |
                 InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T> T[] array(String stmt, Class<T> klass, Object... args) {
        return withConnection(conn -> array(conn, klass, stmt, args));
    }

    public <T> T uniqueOrNull(Connection conn, Class<T> klass, String stmt, Object... args) {
        var result = array(conn, klass, stmt, args);
        return result.length == 0 ? null : result[0];
    }

    public <T> T uniqueOrNull(Class<T> klass, String stmt, Object... args) {
        return withConnection(conn -> uniqueOrNull(conn, klass, stmt, args));
    }

    public Void exec(Connection conn, String stmt, Object... args) {
        try {
            var exprAndOffsets = mapQueryArgs(stmt, args);
            var offsets = exprAndOffsets.offsets;

            System.out.println("### EXEC QUERY:\n" + exprAndOffsets.newExpr);
            //System.out.println("ARGS: " + Arrays.toString(args));

            var procCall = conn.prepareCall(exprAndOffsets.newExpr);

            for (var i = 0; i < offsets.size(); i++) {
                //System.out.println("arg(" + i + ") = " + args[offsets.get(i).argIdx]);
                procCall.setObject(i + 1, args[offsets.get(i).argIdx]);
            }

            procCall.executeUpdate();
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Void exec(String stmt, Object... args) {
        return withConnection(conn -> exec(conn, stmt, args));
    }

    record ArgOffset(int argIdx, int offset) { }

    static int nDigits(int n) {
        int length = 0;
        long temp = 1;
        while (temp <= n) {
            length++;
            temp *= 10;
        }
        return length;
    }

    record ExprAndOffsets(String newExpr, List<ArgOffset> offsets) { }

    public static ExprAndOffsets mapQueryArgs(String expr, Object... args) {
        var offsets = new ArrayList<ArgOffset>();

        for (var i = args.length - 1; i >= 0; i--) {
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

        return new ExprAndOffsets(expr, offsets);
    }

    record FieldInfo(Method accessor, FieldFetcher fetcher) { }

    public static <T> Meta.Mapper<Class<T>, RecordComponent, Constructor<T>, FieldInfo> reflectionMapper() {
        return new Meta.Mapper<>() {
            @Override public String className(Class<T> klass) { return klass.getName(); }
            @Override public String fieldName(RecordComponent field) { return field.getName(); }

            @Override public Stream<RecordComponent> readFields(Class<T> symbol) {
                return Arrays.stream(symbol.getRecordComponents());
            }

            @Override public <A extends Annotation> @Nullable A classAnnotation(
                Class<T> klass, Class<A> annotationClass) {
                return klass.getAnnotation(annotationClass);
            }

            @Override public <A extends Annotation> @Nullable A fieldAnnotation(
                RecordComponent field, Class<A> annotationClass) {
                return field.getAnnotation(annotationClass);
            }

            @Override public Constructor<T> mapClass(Class<T> klass) {
                @SuppressWarnings("unchecked")
                var cons = (Constructor<T>) klass.getDeclaredConstructors()[0];
                cons.setAccessible(true);
                return cons;
            }

            @Override public FieldInfo mapField(RecordComponent field) {
                var accessor = field.getAccessor();
                accessor.setAccessible(true);
                return new FieldInfo(accessor, fetcherForFieldType(field.getType()));
            }
        };
    }

    public <T> T[] select(Connection conn, Class<T> klass, String expr, Object... args) {
        var meta = Meta.parseClass(klass, reflectionMapper());
        var query = QueryBuilder.selectQuery(meta);

        var exprAndOffsets = mapQueryArgs(query + "\n" + expr, args);
        var offsets = exprAndOffsets.offsets;

        try {
            var destQuery = exprAndOffsets.newExpr;
            System.out.println("### EXEC QUERY:\n" + destQuery);
            var pstmt = conn.prepareStatement(destQuery);

            for (var i = 0; i < offsets.size(); i++)
                pstmt.setObject(i + 1, args[offsets.get(i).argIdx]); // gen query

            // base -> java - snake-camel
            // java -> query - came-snake

            pstmt.execute();
            var rs = pstmt.getResultSet(); // snake to camel
            var data = new ArrayList<T>();
            var constructor = meta.info();
            var consArgs = new Object[constructor.getParameters().length];

            while (rs.next()) {
                var fetchArgId = 1;
                for (var i = 0; i < consArgs.length; i++) {
                    var field = meta.fields().get(i);
                    consArgs[i] = field.info().fetcher.fetch(rs, fetchArgId++);
                }

                data.add(constructor.newInstance(consArgs));
            }

            return data.toArray(n -> (T[]) Array.newInstance(klass, n));
        } catch (SQLException | IllegalAccessException | InvocationTargetException |
                 InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T> T[] select(Class<T> klass, String expr, Object... args) {
        return withConnection(conn -> select(conn, klass, expr, args));
    }

    public <T> T[] select(Connection conn, Class<T> klass) {
        return select(conn, klass, "");
    }

    public <T> T[] select(Class<T> klass) {
        return select(klass, "");
    }

    public <T> T selectOne(Class<T> klass, String expr, Object... args) {
        return null;
    }

    public <T> T selectOne(Class<T> klass) {
        return null;
    }

    public <T> T insertSelf(Connection conn, T entity) {
        var meta = Meta.parseClass(entity.getClass(), reflectionMapper());
        var nonJoinedFields = meta.fields().stream()
            .filter(f -> f.atTable() == meta.table()).toList();

        var params = new ArrayList<String>();
        var paramValues = new ArrayList<>();
        var generated = new ArrayList<String>();

        for (var field : nonJoinedFields) {
            if (field.sequence() != null) {
                params.add(field.sequence() + ".nextval");
                generated.add(field.atColumn());
            } else {
                try {
                    paramValues.add(field.info().accessor.invoke(entity));
                    params.add("?");
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        var query = QueryBuilder.insertSelfQuery(meta);

        if (!generated.isEmpty())
            query += " returning " + String.join(", ", generated) + " into " +
                generated.stream().map(it -> "?").collect(Collectors.joining(", "));

        System.out.println("### EXEC QUERY:\n" + query);

        try {
            var cstmt = conn.prepareCall("begin " + query + "; end;");

            for (var i = 0; i < paramValues.size(); i++)
                cstmt.setObject(i + 1, paramValues.get(i));

            for (var i = 0; i < generated.size(); i++)
                cstmt.registerOutParameter(i + paramValues.size() + 1, Types.BIGINT);

            // cstmt.execute();

            var constructor = meta.info();
            var consArgs = new Object[constructor.getParameters().length];
            for (var i = 0; i < consArgs.length; i++) {
                var field = meta.fields().get(i);
                var genIdx = generated.indexOf(field.atColumn());
                if (genIdx != -1)
                    consArgs[i] = cstmt.getLong(paramValues.size() + genIdx + 1);
                else
                    consArgs[i] = field.info().accessor.invoke(entity);
            }
            return (T) constructor.newInstance(consArgs);

        } catch (SQLException | IllegalAccessException | InstantiationException |
                 InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T> T insertSelf(T entity) {
        return withConnection(conn -> insertSelf(conn, entity));
    }

    public <T> Void updateSelf(Connection conn, T entity) {
        var meta = Meta.parseClass(entity.getClass(), reflectionMapper());
        var nonJoinedFields = meta.fields().stream()
            .filter(f -> f.atTable() == meta.table()).toList();
        var noIdFields = nonJoinedFields.stream()
            .filter(f -> !f.isId()).toList();
        var idFields = nonJoinedFields.stream()
            .filter(f -> f.isId()).toList();

        var query = QueryBuilder.updateSelfQuery(meta);

        System.out.println("### EXEC QUERY:\n" + query);

        try {
            var pstmt = conn.prepareStatement(query);

            for (var i = 0; i < noIdFields.size(); i++)
                pstmt.setObject(i + 1, noIdFields.get(i).info().accessor.invoke(entity));

            for (var i = 0; i < idFields.size(); i++)
                pstmt.setObject(noIdFields.size() + i + 1, idFields.get(i).info().accessor.invoke(entity));

            pstmt.execute();

        } catch (SQLException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }

        return null;
    }

    public <T> Void updateSelf(T entity) {
        return withConnection(conn -> updateSelf(conn, entity));
    }

    public <T> Void deleteSelf(Connection conn, T entity) {
        var meta = Meta.parseClass(entity.getClass(), reflectionMapper());
        var idFields = meta.fields().stream()
            .filter(Meta.FieldRef::isId).toList();

        var query = QueryBuilder.deleteSelfQuery(meta);

        System.out.println("### EXEC QUERY:\n" + query);

        try {
            var pstmt = conn.prepareStatement(query);

            for (var i = 0; i < idFields.size(); i++)
                pstmt.setObject(i + 1, idFields.get(i).info().accessor.invoke(entity));

            pstmt.execute();

        } catch (SQLException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }

        return null;
    }

    public <T> Void deleteSelf(T entity) {
        return withConnection(conn -> deleteSelf(conn, entity));
    }
}