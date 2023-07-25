package jstack.ssql;

import jstack.ssql.QueryBuilder.PositionalArgument;
import jstack.ssql.dialect.Bindings;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SafeSql implements ConnectionHandle {
    private static final Logger logger = LoggerFactory.getLogger(SafeSql.class);

    private static final RW deafultRW = new RW() {
        @Override public Object read(ResultSet rs, int i, Class<?> cl) throws SQLException {
            if (cl == byte.class || cl == Byte.class) return rs.getByte(i);
            if (cl == short.class || cl == Short.class) return rs.getShort(i);
            if (cl == int.class || cl == Integer.class) return rs.getInt(i);
            if (cl == long.class || cl == Long.class) return rs.getLong(i);
            if (cl == boolean.class || cl == Boolean.class) return rs.getBoolean(i);
            if (cl == String.class) return rs.getString(i);
            return rs.getObject(i);
        }

        @Override public void write(PreparedStatement stmt, int i, Object v) throws SQLException {
            stmt.setObject(i, v);
        }
    };

    private final DataSource ds;
    private final Connection connection;
    private final Map<Class<?>, RW> userRW;

    public SafeSql(DataSource ds) {
        this(Class.class, ds); // no dialect
    }

    public SafeSql(Class<?> dialect, DataSource ds) {
        this.ds = ds;
        this.connection = null;
        this.userRW = new HashMap<>();

        for (var bind : dialect.getAnnotation(Bindings.class).value()) {
            try {
                userRW.put(bind.klass(), bind.using().newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected SafeSql(
        DataSource ds, Connection connection, Map<Class<?>, RW> userRW
    ) {
        this.ds = ds;
        this.connection = connection;
        this.userRW = userRW;
    }

    record FieldInfo(Method accessor, Class<?> type, RW rw) { }

    private <T> Mapper<Class<T>, RecordComponent, Constructor<T>, FieldInfo> reflectionMapper() {
        return new Mapper<>() {
            @Override public String className(Class<T> klass) { return klass.getName(); }
            @Override public String fieldName(RecordComponent field) { return field.getName(); }

            @Override public Stream<RecordComponent> readFields(Class<T> symbol) {
                return Arrays.stream(symbol.getRecordComponents());
            }

            @Override public <A extends Annotation> @Nullable A classAnnotation(
                Class<T> klass, Class<A> annotationClass
            ) {
                return klass.getAnnotation(annotationClass);
            }

            @Override public <A extends Annotation> @Nullable A fieldAnnotation(
                RecordComponent field, Class<A> annotationClass
            ) {
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
                    accessor, field.getType(), userRW.getOrDefault(field.getType(), deafultRW)
                );
            }
        };
    }

    /* FIXME: may handler throw Exception ??? */
    public <T> T withConnection(Function<ConnectionHandle, T> handler) {
        try (var conn = ds.getConnection()) {
            return handler.apply(new SafeSql(ds, conn, userRW));
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override public <T> T withTransaction(Function<TransactionHandle, T> handler) {
        throw new RuntimeException("Not implemented yet");
    }

    private <T> T inConnection(Function<Connection, T> handler) {
        try (var conn = ds.getConnection()) {
            return handler.apply(conn);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override public <T> T exec(@Language("sql") String stmt, Class<T> toClass, Object... args) {
        Function<Connection, T> doExec = conn -> {
            var query = QueryBuilder.forExec(stmt, args.length);
            logger.debug("EXEC QUERY:\n {}", query.text);

            try {
                var procCall = conn.prepareCall(query.text);

                for (var i = 0; i < query.arguments.size(); i++) {
                    var javaArg = args[query.arguments.get(i).javaArgumentIndex];
                    var rw = userRW.getOrDefault(javaArg.getClass(), deafultRW);
                    rw.write(procCall, i + 1, javaArg);
                }

                procCall.executeUpdate();
                return null;

            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        };

        return connection != null
            ? doExec.apply(connection) : inConnection(doExec);
    }

    private ResultSet prepareAndExec(
        Connection conn, String queryText, List<PositionalArgument> arguments, Object[] javaArgs
    ) throws SQLException {
        var pstmt = conn.prepareStatement(queryText);

        for (var i = 0; i < arguments.size(); i++) {
            var javaArg = javaArgs[arguments.get(i).javaArgumentIndex];
            var rw = userRW.getOrDefault(javaArg.getClass(), deafultRW);
            rw.write(pstmt, i + 1, javaArg);
        }

        pstmt.execute();
        return pstmt.getResultSet();
    }

    @Override public <T> Stream<T> queryAsStream(String stmt, Class<T> toClass, Object... args) {
        BiFunction<Connection, Boolean, Stream<T>> doQuery = (conn, isNewConnection) -> {
            try {
                var isRecord = toClass.isRecord();
                var queryTuple = isRecord
                    ? QueryBuilder.forQueryTuple(Arrays.stream(toClass.getRecordComponents())
                        .map(reflectionMapper()::mapField).toList(), stmt, args.length)
                    : null;
                var queryScalar = !isRecord
                    ? QueryBuilder.forQueryScalar(toClass, stmt, args.length)
                    : null;

                logger.debug("EXEC QUERY:\n {}", isRecord ? queryTuple : queryScalar);

                var rs = isRecord ? prepareAndExec(conn, queryTuple.text, queryTuple.arguments, args)
                    : prepareAndExec(conn, queryScalar.text, queryScalar.arguments, args);

                @SuppressWarnings("unchecked")
                var cons = (Constructor<T>) toClass.getDeclaredConstructors()[0];
                cons.setAccessible(true);
                var consArgs = new Object[cons.getParameters().length];

                return StreamSupport.stream(new Spliterators.AbstractSpliterator<T>(
                    Long.MAX_VALUE, Spliterator.ORDERED) {
                    @Override
                    public boolean tryAdvance(Consumer<? super T> action) {
                        try {
                            if (!rs.next()) return false;

                            if (isRecord) {
                                for (var i = 0; i < queryTuple.results.size(); i++) {
                                    var field = queryTuple.results.get(i).field;
                                    consArgs[i] = field.rw.read(rs, i + 1, field.type);
                                }

                                action.accept(cons.newInstance(consArgs));
                            } else
                                action.accept((T) rs.getObject(1));

                            return true;
                        } catch (SQLException | InvocationTargetException |
                                 InstantiationException |
                                 IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, false).onClose(() -> {
                    try {
                        rs.close();
                        if (isNewConnection)
                            conn.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };

        try {
            return connection == null
                ? doQuery.apply(ds.getConnection(), true)
                : doQuery.apply(connection, false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public <T> List<T> queryAsList(String stmt, Class<T> toClass, Object... args) {
        try (var stream = queryAsStream(stmt, toClass, args)) {
            return stream.toList();
        }
    }

    @Override public <T> T[] query(String stmt, Class<T> toClass, Object... args) {
        try (var stream = queryAsStream(stmt, toClass, args)) {
            return stream.toArray(n -> (T[]) Array.newInstance(toClass, n));
        }
    }

    @Override
    public <T> T queryOne(@Language("sql") String stmt, Class<T> toClass, Object... args) {
        var result = query(stmt, toClass, args);
        if (result.length != 1) throw new RuntimeException(
            "expected exactly 1 row but has " + result.length
        );

        return result[0];
    }

    @Override @Nullable public <T> T queryOneOrNull(
        @Language("sql") String stmt, Class<T> toClass, Object... args
    ) {
        var result = query(stmt, toClass, args);
        if (result.length > 1) throw new RuntimeException(
            "expected 0 or 1 row but has " + result.length
        );

        return result.length == 0 ? null : result[0];
    }
}
