package com.over64;

import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
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
    public @interface Id {
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Joined {
    }


    @Retention(RetentionPolicy.RUNTIME)
    public @interface Inner {
        String value();
    }


    final Sql2o db;

    public TypesafeSql(DataSource ds) {
        db = new Sql2o(ds);
    }

    Query queryWithParams(Connection conn, String stmt, boolean returnGeneratedKeys, List<Object> args) {
        for (var i = 0; i < args.size(); i++)
            stmt = stmt.replace(":" + (i + 1), ":p" + (i + 1));

        var query = conn.createQuery(stmt, returnGeneratedKeys);

        for (var i = 0; i < args.size(); i++)
            query.addParameter("p" + (i + 1), args.get(i));

        return query;
    }

    interface Fetcher {
        Object fetch(String paramName) throws SQLException;
    }

    interface FetcherByIdx {
        Object fetch(int idx) throws SQLException;
    }

    FetcherByIdx fetchByTypeRefByIdx(Class<?> klass, ResultSet rs) {
        if (klass.equals(long.class) || klass.equals(Long.class)) return rs::getLong;
        else if (klass.equals(int.class) || klass.equals(Integer.class)) return rs::getInt;

        return rs::getObject;
    }

    Fetcher fetchByTypeRef(Class<?> klass, ResultSet rs) {
        if (klass.equals(long.class) || klass.equals(Long.class)) return rs::getLong;
        else if (klass.equals(int.class) || klass.equals(Integer.class)) return rs::getInt;

        return rs::getObject;
    }

    public <T> List<T> list(String stmt, Class<T> klass, Object... args) {
        try (var conn = db.open()) {
            var query = queryWithParams(conn, stmt, false, Arrays.asList(args));

            if (klass.isRecord()) {
                var data = query.executeAndFetch(new ResultSetHandler<T>() {
                    @Override
                    public T handle(ResultSet rs) throws SQLException {
                        var constuctor = klass.getDeclaredConstructors()[0];
                        constuctor.setAccessible(true);
                        var consArgs = new Object[constuctor.getParameters().length];
                        for (var i = 0; i < consArgs.length; i++) {
                            var param = constuctor.getParameters()[i];
                            consArgs[i] = fetchByTypeRef(param.getType(), rs).fetch(param.getName());
                        }
                        try {
                            return (T) constuctor.newInstance(consArgs);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
                return data;
            } else {
                return query.executeAndFetch(klass);
            }
        }
    }

    public <T> T[] array(String stmt, Class<T> klass, Object... args) {
        return (T[]) list(stmt, klass, args).toArray((T[]) Array.newInstance(klass, 0));
    }

    public <T> T uniqueOrNull(String stmt, Class<T> klass, Object... args) {
        var result = list(stmt, klass, args);
        return result.isEmpty() ? null : result.get(0);
    }

    public Void exec(String stmt, Object... args) {
        try (var conn = db.open()) {
            queryWithParams(conn, stmt, false, Arrays.asList(args)).executeUpdate();
            return null;
        }
    }

    record ArgOffset(int argIdx, int offset) {}

    int nDigits(int n) {
        int length = 0;
        long temp = 1;
        while (temp <= n) {
            length++;
            temp *= 10;
        }
        return length;
    }

    public <T> T[] select(Class<T> klass, String expr, Object... args) {
        // FIXME: check that @Table and @Joined annotations do not used both
        if (klass.getAnnotation(Table.class) != null) {
            var tableName = klass.getAnnotation(Table.class).value();
            return array("select * from " + tableName + " " + expr, klass, args);
        } else if (klass.getAnnotation(Joined.class) != null) {
            var query =
                "select " + Arrays.stream(klass.getRecordComponents())
                    .map(field -> Arrays.stream(field.getType().getRecordComponents())
                        .map(innerField -> field.getName() + "." + innerField.getName())
                        .collect(Collectors.joining(", ")))
                    .collect(Collectors.joining(", ")) +
                    " from " + Arrays.stream(klass.getRecordComponents())
                    .map(field -> {
                        var fieldClass = field.getType();
                        var table = fieldClass.getAnnotation(Table.class).value();
                        var joinedInner = field.getAnnotation(Inner.class);
                        var tableAndName = table + " " + field.getName();

                        if (joinedInner != null)
                            return "inner join " + tableAndName + " on " + joinedInner.value();

                        return tableAndName;
                    })
                    .collect(Collectors.joining(" "));

            var offsets = new ArrayList<ArgOffset>();

            for (var i = 0; i < args.length; i++) {
                for (; ; ) {
                    var offset = expr.indexOf(":" + (i + 1));
                    var iLength = nDigits(i + 1);
                    if (offset == -1) break;
                    expr = expr.substring(0, offset) + "?" + " ".repeat(iLength) + expr.substring(offset + iLength + 1);
                    offsets.add(new ArgOffset(i, offset));
                }
            }

            offsets.sort(Comparator.comparingInt(o -> o.offset));

            try (var conn = db.open()) {
                var jdbcConn = conn.getJdbcConnection();
                var pstmt = jdbcConn.prepareStatement(query + " " + expr);

                for (var i = 0; i < offsets.size(); i++)
                    pstmt.setObject(i + 1, args[offsets.get(i).argIdx]);

                pstmt.execute();
                var rs = pstmt.getResultSet();
                var data = new ArrayList<T>();
                var constructor = klass.getDeclaredConstructors()[0];
                var consArgs = new Object[constructor.getParameters().length];
                constructor.setAccessible(true);

                while (rs.next()) {
                    var fetchArgId = 1;
                    for (var i = 0; i < consArgs.length; i++) {
                        var param = constructor.getParameters()[i];
                        var joinedClass = param.getType();
                        var joinedConstructor = joinedClass.getDeclaredConstructors()[0];
                        var joinedConsArgs = new Object[joinedConstructor.getParameters().length];
                        joinedConstructor.setAccessible(true);

                        for (var j = 0; j < joinedConsArgs.length; j++)
                            joinedConsArgs[j] = fetchByTypeRefByIdx(joinedConstructor.getParameters()[j].getType(), rs)
                                .fetch(fetchArgId++);

                        consArgs[i] = joinedConstructor.newInstance(joinedConsArgs);
                    }

                    data.add((T) constructor.newInstance(consArgs));
                }

                return data.toArray(n -> (T[]) Array.newInstance(klass, n));
            } catch (SQLException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
                throw new RuntimeException(ex);
            }
        }

        throw new RuntimeException("selected entity class must be annotated as @Table or @Joined");
    }

    public interface Ref<A, B> {
        B supply(A instance);
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

    public <T> T insert(T entity) {
        var tableName = entity.getClass().getAnnotation(Table.class).value();
        var fields = entity.getClass().getRecordComponents();

        var values = Stream.of(fields)
            .map(RecordComponent::getName)
            .collect(Collectors.joining(", ", "(", ")"));

        var params = new ArrayList<String>();
        var paramValues = new ArrayList<>();
        var generated = new ArrayList<String>();

        for (var field : fields) {
            var sequenceAnnotation = field.getAnnotation(Sequence.class);
            if (sequenceAnnotation != null) {
                params.add(sequenceAnnotation.value() + ".nextval");
                generated.add(field.getName());
            } else {
                try {
                    var accessor = field.getAccessor();
                    accessor.setAccessible(true);
                    paramValues.add(accessor.invoke(entity));
                    params.add("?");
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        var query = "insert into " + tableName + values + " values " +
            params.stream().collect(Collectors.joining(", ", "(", ")"));

        if (!generated.isEmpty())
            query += " returning " + String.join(", ", generated) + " into " +
                generated.stream().map(it -> "?").collect(Collectors.joining(", "));

        try (var conn = db.open()) {
            var jdbcConn = conn.getJdbcConnection();
            var cstmt = jdbcConn.prepareCall("begin " + query + "; end;");

            for (var i = 0; i < paramValues.size(); i++)
                cstmt.setObject(i + 1, paramValues.get(i));

            for (var i = 0; i < generated.size(); i++)
                cstmt.registerOutParameter(i + paramValues.size() + 1, Types.BIGINT);

            cstmt.execute();

            var constuctor = entity.getClass().getDeclaredConstructors()[0];
            constuctor.setAccessible(true);
            var consArgs = new Object[constuctor.getParameters().length];
            for (var i = 0; i < consArgs.length; i++) {
                var param = constuctor.getParameters()[i];
                var name = param.getName();
                var genIdx = generated.indexOf(name);
                if (genIdx != -1)
                    consArgs[i] = cstmt.getLong(paramValues.size() + genIdx + 1);
                else {
                    var accessor = fields[i].getAccessor();
                    accessor.setAccessible(true);
                    consArgs[i] = accessor.invoke(entity);
                }
            }
            return (T) constuctor.newInstance(consArgs);

        } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T> Void update(T entity) {
        var tableName = entity.getClass().getAnnotation(Table.class).value();
        var fields = entity.getClass().getRecordComponents();
        var noIdFields = Arrays.stream(fields)
            .filter(f -> !f.isAnnotationPresent(Id.class))
            .collect(Collectors.toList());
        var idFields = Arrays.stream(fields)
            .filter(f -> f.isAnnotationPresent(Id.class))
            .collect(Collectors.toList());

        var query = "update " + tableName
            + " set " +
            noIdFields.stream()
                .map(f -> f.getName() + " = ?")
                .collect(Collectors.joining(", ")) +
            " where " +
            idFields.stream().map(f -> f.getName() + " = ?")
                .collect(Collectors.joining(" and "));

        try (var conn = db.open()) {
            var jdbcConn = conn.getJdbcConnection();
            var pstmt = jdbcConn.prepareStatement(query);

            for (var i = 0; i < noIdFields.size(); i++) {
                var accessor = noIdFields.get(i).getAccessor();
                accessor.setAccessible(true);
                pstmt.setObject(i + 1, accessor.invoke(entity));
            }

            for (var i = 0; i < idFields.size(); i++) {
                var accessor = idFields.get(i).getAccessor();
                accessor.setAccessible(true);
                pstmt.setObject(noIdFields.size() + i + 1, accessor.invoke(entity));
            }

            pstmt.execute();

        } catch (SQLException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }

        return null;
    }

    public <T> Void delete(T entity) {
        var tableName = entity.getClass().getAnnotation(Table.class).value();
        var idFields = Arrays.stream(entity.getClass().getRecordComponents())
            .filter(f -> f.isAnnotationPresent(Id.class))
            .collect(Collectors.toList());

        var query = "delete from " + tableName + " where " +
            idFields.stream().map(f -> f.getName() + " = ?")
                .collect(Collectors.joining(" and "));

        try (var conn = db.open()) {
            var jdbcConn = conn.getJdbcConnection();
            var pstmt = jdbcConn.prepareStatement(query);

            for (var i = 0; i < idFields.size(); i++) {
                var accessor = idFields.get(i).getAccessor();
                accessor.setAccessible(true);
                pstmt.setObject(i + 1, accessor.invoke(entity));
            }

            pstmt.execute();

        } catch (SQLException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }

        return null;
    }
}

