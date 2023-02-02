package jstack.ssql;

import jstack.ssql.QueryBuilder.PositionalArgument;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class SafeSql {

    interface FieldFetcher {
        Object fetch(ResultSet rs, int idx) throws SQLException;
    }

    static FieldFetcher fetcherForFieldType(Class<?> fieldType) {
        if (fieldType == byte.class || fieldType == Byte.class) return ResultSet::getByte;
        if (fieldType == short.class || fieldType == Short.class) return ResultSet::getShort;
        if (fieldType == int.class || fieldType == Integer.class) return ResultSet::getInt;
        if (fieldType == long.class || fieldType == Long.class) return ResultSet::getLong;
        if (fieldType == boolean.class || fieldType == Boolean.class) return ResultSet::getBoolean;
        if (fieldType == String.class) return ResultSet::getString;
        return ResultSet::getObject;
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

    private final DataSource ds;
    public SafeSql(DataSource ds) {
        this.ds = ds;
    }


    /* FIXME: may handler throw Exception ??? */
    public <T> T withConnection(Function<Connection, T> handler) {
        try (var conn = ds.getConnection()) {
            return handler.apply(conn);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Void exec(Connection conn, @Language("sql") String stmt, Object... args) {
        var query = QueryBuilder.forExec(stmt, args.length);
        System.out.println("### EXEC QUERY:\n" + query.text);

        try {
            var procCall = conn.prepareCall(query.text);

            for (var i = 0; i < query.arguments.size(); i++)
                procCall.setObject(i + 1, args[query.arguments.get(i).javaArgumentIndex]);

            procCall.executeUpdate();
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Void exec(@Language("sql") String stmt, Object... args) {
        return withConnection(conn -> exec(conn, stmt, args));
    }

    private ResultSet prepareAndExec(
        Connection conn, String queryText, List<PositionalArgument> arguments, Object[] javaArgs
    ) throws SQLException {
        var pstmt = conn.prepareStatement(queryText);

        for (var i = 0; i < arguments.size(); i++)
            pstmt.setObject(i + 1, javaArgs[arguments.get(i).javaArgumentIndex]);

        pstmt.execute();
        return pstmt.getResultSet();
    }

    public <T> T[] query(Connection conn, Class<T> klass, @Language("sql") String stmt, Object... args) {
        var data = new ArrayList<T>();

        try {
            if (klass.isRecord()) {
                var query = QueryBuilder.forQueryTuple(
                    Arrays.asList(klass.getRecordComponents()), stmt, args.length
                );
                System.out.println("### EXEC QUERY:\n" + query.text);

                var rs = prepareAndExec(conn, query.text, query.arguments, args);

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
            } else {
                var query = QueryBuilder.forQueryScalar(klass, stmt, args.length);
                System.out.println("### EXEC QUERY:\n" + query.text);

                var rs = prepareAndExec(conn, query.text, query.arguments, args);

                while (rs.next())
                    data.add((T) rs.getObject(1));
            }
        } catch (SQLException | IllegalAccessException | InvocationTargetException |
                 InstantiationException ex) {
            throw new RuntimeException(ex);
        }

        return data.toArray(n -> (T[]) Array.newInstance(klass, n));
    }

    public <T> T[] query(Class<T> klass, @Language("sql") String stmt, Object... args) {
        return withConnection(conn -> query(conn, klass, stmt, args));
    }

    public <T> T queryOne(Connection conn, Class<T> klass, @Language("sql") String stmt, Object... args) {
        return null;
    }

    public <T> T queryOne(Class<T> klass, @Language("sql") String stmt, Object... args) {
        return withConnection(conn -> queryOneOrNull(conn, klass, stmt, args));
    }

    // TODO: rename to queryOneOrNull, fix impl
    public <T> T queryOneOrNull(Connection conn, Class<T> klass, @Language("sql") String stmt, Object... args) {
        var result = query(conn, klass, stmt, args);
        return result.length == 0 ? null : result[0];
    }

    public <T> T queryOneOrNull(Class<T> klass, @Language("sql") String stmt, Object... args) {
        return withConnection(conn -> queryOneOrNull(conn, klass, stmt, args));
    }

    public <T> T[] select(Connection conn, Class<T> klass, @Language("sql") String expr, Object... args) {
        var meta = Meta.parseClass(klass, reflectionMapper());
        var query = QueryBuilder.forSelect(meta, expr, args.length);
        System.out.println("### EXEC QUERY:\n" + query.text);

        try {
            var pstmt = conn.prepareStatement(query.text);

            for (var i = 0; i < query.arguments.size(); i++)
                pstmt.setObject(i + 1, args[query.arguments.get(i).javaArgumentIndex]);

            pstmt.execute();
            var rs = pstmt.getResultSet();
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

    public <T> T[] select(Class<T> klass, @Language("sql") String expr, Object... args) {
        return withConnection(conn -> select(conn, klass, expr, args));
    }

    public <T> T[] select(Connection conn, Class<T> klass) {
        return select(conn, klass, "");
    }

    public <T> T[] select(Class<T> klass) {
        return select(klass, "");
    }

    public <T> T selectOne(Connection conn, Class<T> klass, @Language("sql") String expr, Object... args) {
        return null;
    }

    public <T> T selectOne(Class<T> klass, @Language("sql") String expr, Object... args) {
        return null;
    }

    @Nullable public <T> T selectOneOrNull(
        Connection conn, Class<T> klass, @Language("sql") String expr, Object... args
    ) {
        return null;
    }

    @Nullable public <T> T selectOneOrNull(
        Class<T> klass, @Language("sql") String expr, Object... args
    ) {
        return null;
    }

    public <T> T insertSelf(Connection conn, T entity) {
        var meta = Meta.parseClass(entity.getClass(), reflectionMapper());
        var query = QueryBuilder.forInsertSelf(meta);
        System.out.println("### EXEC QUERY:\n" + query.text);

        try {
            // FOR ORACLE
//            var cstmt = conn.prepareCall(query.text);
//
//            for (var i = 0; i < query.arguments.size(); i++)
//                cstmt.setObject(i + 1, query.arguments.get(i).field.accessor.invoke(entity));
//
//            // FIXME: correct support for out parameters types
//            for (var i = 0; i < query.results.size(); i++)
//                cstmt.registerOutParameter(i + query.arguments.size() + 1, Types.BIGINT);
//
//            cstmt.execute();

            var cstmt = conn.prepareStatement(query.text);

            for (var i = 0; i < query.arguments.size(); i++)
                cstmt.setObject(i + 1, query.arguments.get(i).field.accessor.invoke(entity));

            cstmt.execute();
            var rs = cstmt.getResultSet();
            rs.next();

            var constructor = meta.info();
            var consArgs = new Object[constructor.getParameters().length];

            for (var i = 0; i < consArgs.length; i++) {
                var fieldRef = meta.fields().get(i);
                var result = query.results.stream()
                    .filter(r -> r.field == fieldRef.info())
                    .findFirst().orElse(null);

                if (result != null)
                    consArgs[i] = result.field.fetcher.fetch(rs, result.sqlColumnNumber);
                else
                    consArgs[i] = fieldRef.info().accessor.invoke(entity);
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
        var query = QueryBuilder.forUpdateSelf(meta);
        System.out.println("### EXEC QUERY:\n" + query.text);

        try {
            var pstmt = conn.prepareStatement(query.text);

            for (var i = 0; i < query.arguments.size(); i++)
                pstmt.setObject(i + 1, query.arguments.get(i).field.accessor.invoke(entity));

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
        var query = QueryBuilder.forDeleteSelf(meta);

        System.out.println("### EXEC QUERY:\n" + query.text);

        try {
            var pstmt = conn.prepareStatement(query.text);

            for (var i = 0; i < query.arguments.size(); i++)
                pstmt.setObject(i + 1, query.arguments.get(i).field.accessor.invoke(entity));

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