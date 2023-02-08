package jstack.ssql;

import jstack.ssql.QueryBuilder.PositionalArgument;
import jstack.ssql.schema.Ordinal;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.lang.reflect.Array;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class SafeSql {
    interface RSReader {
        Object read(ResultSet rs, int idx) throws SQLException;
    }

    interface RSWriter {
        void write(PreparedStatement stmt, int idx, Object value) throws SQLException;
    }

    static RSReader readerForType(Class<?> type) {
        if (type.isEnum())
            if (type.getAnnotation(Ordinal.class) != null)
                return (rs, i) -> type.getEnumConstants()[rs.getInt(i)];
            else
                return (rs, i) -> {
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    var result = Enum.valueOf((Class<? extends Enum>) type, rs.getString(i));
                    return result;
                };

        if (type == byte.class || type == Byte.class) return ResultSet::getByte;
        if (type == short.class || type == Short.class) return ResultSet::getShort;
        if (type == int.class || type == Integer.class) return ResultSet::getInt;
        if (type == long.class || type == Long.class) return ResultSet::getLong;
        if (type == boolean.class || type == Boolean.class) return ResultSet::getBoolean;
        if (type == String.class) return ResultSet::getString;
        return ResultSet::getObject;
    }

    static RSWriter writerForType(Class<?> type) {
        if (type.isEnum()) {
            if (type.getAnnotation(Ordinal.class) != null)
                return (stmt, idx, value) ->
                    stmt.setObject(idx, ((Enum<?>) value).ordinal());
            else
                return (stmt, idx, value) ->
                    stmt.setObject(idx, ((Enum<?>) value).name());
        } else
            return PreparedStatement::setObject;
    }

    record FieldInfo(Method accessor, RSReader reader, RSWriter writer) { }

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
                return new FieldInfo(
                    accessor, readerForType(field.getType()),
                    writerForType(field.getType())
                );
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

            for (var i = 0; i < query.arguments.size(); i++) {
                var javaArg = args[query.arguments.get(i).javaArgumentIndex];
                var writer = writerForType(javaArg.getClass());
                writer.write(procCall, i + 1, javaArg);
            }

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

        for (var i = 0; i < arguments.size(); i++) {
            var javaArg = javaArgs[arguments.get(i).javaArgumentIndex];
            var writer = writerForType(javaArg.getClass());
            writer.write(pstmt, i + 1, javaArg);
        }

        pstmt.execute();
        return pstmt.getResultSet();
    }

    public <T> T[] query(Connection conn, Class<T> klass, @Language("sql") String stmt, Object... args) {
        var data = new ArrayList<T>();

        try {
            if (klass.isRecord()) {
                var mapper = reflectionMapper();
                var query = QueryBuilder.forQueryTuple(
                    Arrays.stream(klass.getRecordComponents())
                        .map(mapper::mapField).toList(), stmt, args.length
                );
                System.out.println("### EXEC QUERY:\n" + query.text);

                var rs = prepareAndExec(conn, query.text, query.arguments, args);

                @SuppressWarnings("unchecked")
                var cons = (Constructor<T>) klass.getDeclaredConstructors()[0];
                cons.setAccessible(true);
                var consArgs = new Object[cons.getParameters().length];

                while (rs.next()) {
                    for (var i = 0; i < query.results.size(); i++) {
                        var field = query.results.get(i).field;
                        consArgs[i] = field.reader.read(rs, i + 1);
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

            for (var i = 0; i < query.arguments.size(); i++) {
                var javaArg = args[query.arguments.get(i).javaArgumentIndex];
                var writer = writerForType(javaArg.getClass());
                writer.write(pstmt, i + 1, javaArg);
            }

            pstmt.execute();
            var rs = pstmt.getResultSet();
            var data = new ArrayList<T>();
            var constructor = meta.info();
            var consArgs = new Object[constructor.getParameters().length];

            while (rs.next()) {
                var fetchArgId = 1;
                for (var i = 0; i < consArgs.length; i++) {
                    var field = meta.fields().get(i);
                    consArgs[i] = field.info().reader.read(rs, fetchArgId++);
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

            for (var i = 0; i < query.arguments.size(); i++) {
                var field = query.arguments.get(i).field;
                field.writer.write(cstmt, i + 1, field.accessor.invoke(entity));
            }

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
                    consArgs[i] = result.field.reader.read(rs, result.sqlColumnNumber);
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

            for (var i = 0; i < query.arguments.size(); i++) {
                var field = query.arguments.get(i).field;
                field.writer.write(pstmt, i + 1, field.accessor.invoke(entity));
            }

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

            for (var i = 0; i < query.arguments.size(); i++) {
                var field = query.arguments.get(i).field;
                field.writer.write(pstmt, i + 1, field.accessor.invoke(entity));
            }

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