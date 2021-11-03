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
    public @interface Id {}

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

    Fetcher fetchByTypeRef(Class<?> klass, ResultSet rs) {
        if (klass.equals(long.class) || klass.equals(Long.class)) return rs::getLong;
        else if (klass.equals(int.class) || klass.equals(Integer.class)) return rs::getInt;

        return rs::getObject;
    }

    /* FIXME: may handler throw Exception ??? */
    public <T> T withConnection(Function<Connection, T> handler) {
        try (var conn = db.open()) {
            return handler.apply(conn);
        }
    }

    <T> List<T> list(Connection conn, String stmt, Class<T> klass, Object... args) {
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


    public <T> T[] array(Connection conn, String stmt, Class<T> klass, Object... args) {
        return list(conn, stmt, klass, args).toArray((T[]) Array.newInstance(klass, 0));
    }

    public <T> T[] array(String stmt, Class<T> klass, Object... args) {
        try (var conn = db.open()) {
            return array(conn, stmt, klass, args);
        }
    }

    public <T> T uniqueOrNull(Connection conn, String stmt, Class<T> klass, Object... args) {
        var result = list(conn, stmt, klass, args);
        return result.isEmpty() ? null : result.get(0);
    }

    public <T> T uniqueOrNull(String stmt, Class<T> klass, Object... args) {
        try (var conn = db.open()) {
            return uniqueOrNull(conn, stmt, klass, args);
        }
    }

    public Void call(Connection conn, String stmt, Object... args) {
        var jconn = conn.getJdbcConnection();
        try {
            var exprAndOffsets = mapQueryArgs("call " + stmt, args);
            var offsets = exprAndOffsets.offsets;

            var procCall = jconn.prepareCall(exprAndOffsets.newExpr);

            for (var i = 0; i < offsets.size(); i++)
                procCall.setObject(i + 1, args[offsets.get(i).argIdx]);

            procCall.executeUpdate();
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Void call(String stmt, Object... args) {
        try (var conn = db.open()) {
            return call(conn, stmt, args);
        }
    }

    public Void exec(Connection conn, String stmt, Object... args) {
        queryWithParams(conn, stmt, false, Arrays.asList(args)).executeUpdate();
        return null;
    }

    public Void exec(String stmt, Object... args) {
        try (var conn = db.open()) {
            return exec(conn, stmt, args);
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

    record ExprAndOffsets(String newExpr, List<ArgOffset> offsets) {}

    public ExprAndOffsets mapQueryArgs(String expr, Object... args) {
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

        return new ExprAndOffsets(expr, offsets);
    }

    public <T> T[] select(Connection conn, Class<T> klass, String expr, Object... args) {
        var meta = Meta.parseClass(klass);
        var fromTable = meta.table();
        var query = " select\n\t%s\n from\n\t%s\n\t%s".formatted(
            meta.fields().stream().map(f ->
                    (f.atTable().alias() != null ? f.atTable().alias() + "." : "") + f.atColumn())
                .collect(Collectors.joining(",\n\t")),
            fromTable.name() + (fromTable.alias() != null ? " " + fromTable.alias() : ""),
            meta.joins().stream()
                .map(join -> {
                    var kind = join.mode().toString().toLowerCase() + " join";
                    var tableExpr = " " + join.table().name() +
                        (join.table().alias() != null ? " " + join.table().alias() : "");

                    return kind + tableExpr + " on " + join.onExpr();
                }).collect(Collectors.joining("\n\t")));

        var exprAndOffsets = mapQueryArgs(expr, args);
        var offsets = exprAndOffsets.offsets;

        try {
            var jdbcConn = conn.getJdbcConnection();
            var destQuery = query + "\n" + exprAndOffsets.newExpr;
            System.out.println("### EXEC QUERY:\n" + destQuery);
            var pstmt = jdbcConn.prepareStatement(destQuery);

            for (var i = 0; i < offsets.size(); i++)
                pstmt.setObject(i + 1, args[offsets.get(i).argIdx]);

            pstmt.execute();
            var rs = pstmt.getResultSet();
            var data = new ArrayList<T>();
            var constructor = meta.cons();
            var consArgs = new Object[constructor.getParameters().length];

            while (rs.next()) {
                var fetchArgId = 1;
                for (var i = 0; i < consArgs.length; i++) {
                    var field = meta.fields().get(i);
                    consArgs[i] = field.fetcher().fetch(rs, fetchArgId++);
                }

                data.add((T) constructor.newInstance(consArgs));
            }

            return data.toArray(n -> (T[]) Array.newInstance(klass, n));
        } catch (SQLException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T> T[] select(Class<T> klass, String expr, Object... args) {
        try (var conn = db.open()) {
            return select(conn, klass, expr, args);
        }
    }

    public <T> T[] select(Connection conn, Class<T> klass) {
        return select(conn, klass, "");
    }

    public <T> T[] select(Class<T> klass) {
        try (var conn = db.open()) {
            return select(conn, klass);
        }
    }

    public <T> T selectOne(Class<T> klass, String expr, Object... args) {
        return null;
    }

    public <T> T selectOne(Class<T> klass) {
        return null;
    }

    public <T> T insertSelf(T entity) {
        var meta = Meta.parseClass(entity.getClass());
        var nonJoinedFields = meta.fields().stream()
            .filter(f -> f.atTable() == meta.table()).toList();

        var values = nonJoinedFields.stream()
            .map(Meta.FieldRef::atColumn)
            .collect(Collectors.joining(", ", "(", ")"));

        var params = new ArrayList<String>();
        var paramValues = new ArrayList<>();
        var generated = new ArrayList<String>();

        for (var field : nonJoinedFields) {
            if (field.sequence() != null) {
                params.add(field.sequence() + ".nextval");
                generated.add(field.atColumn());
            } else {
                try {
                    paramValues.add(field.accessor().invoke(entity));
                    params.add("?");
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        var query = "insert into " + meta.table().name() + values + " values " +
            params.stream().collect(Collectors.joining(", ", "(", ")"));

        if (!generated.isEmpty())
            query += " returning " + String.join(", ", generated) + " into " +
                generated.stream().map(it -> "?").collect(Collectors.joining(", "));

        System.out.println("### EXEC QUERY:\n" + query);

        try (var conn = db.open()) {
            var jdbcConn = conn.getJdbcConnection();
            var cstmt = jdbcConn.prepareCall("begin " + query + "; end;");

            for (var i = 0; i < paramValues.size(); i++)
                cstmt.setObject(i + 1, paramValues.get(i));

            for (var i = 0; i < generated.size(); i++)
                cstmt.registerOutParameter(i + paramValues.size() + 1, Types.BIGINT);

            cstmt.execute();

            var constuctor = meta.cons();
            var consArgs = new Object[constuctor.getParameters().length];
            for (var i = 0; i < consArgs.length; i++) {
                var field = meta.fields().get(i);
                var genIdx = generated.indexOf(field.atColumn());
                if (genIdx != -1)
                    consArgs[i] = cstmt.getLong(paramValues.size() + genIdx + 1);
                else
                    consArgs[i] = field.accessor().invoke(entity);
            }
            return (T) constuctor.newInstance(consArgs);

        } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T> Void updateSelf(T entity) {
        var meta = Meta.parseClass(entity.getClass());
        var nonJoinedFields = meta.fields().stream()
            .filter(f -> f.atTable() == meta.table()).toList();
        var noIdFields = nonJoinedFields.stream()
            .filter(f -> !f.isId()).toList();
        var idFields = nonJoinedFields.stream()
            .filter(f -> f.isId()).toList();

        var query = "update " + meta.table().name()
            + " set " +
            noIdFields.stream()
                .map(f -> f.atColumn() + " = ?")
                .collect(Collectors.joining(", ")) +
            " where " +
            idFields.stream().map(f -> f.atColumn() + " = ?")
                .collect(Collectors.joining(" and "));

        System.out.println("### EXEC QUERY:\n" + query);

        try (var conn = db.open()) {
            var jdbcConn = conn.getJdbcConnection();
            var pstmt = jdbcConn.prepareStatement(query);

            for (var i = 0; i < noIdFields.size(); i++)
                pstmt.setObject(i + 1, noIdFields.get(i).accessor().invoke(entity));

            for (var i = 0; i < idFields.size(); i++)
                pstmt.setObject(noIdFields.size() + i + 1, idFields.get(i).accessor().invoke(entity));

            pstmt.execute();

        } catch (SQLException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }

        return null;
    }

    public <T> Void deleteSelf(T entity) {
        var meta = Meta.parseClass(entity.getClass());
        var idFields = meta.fields().stream()
            .filter(Meta.FieldRef::isId).toList();

        var query = "delete from " + meta.table().name() + " where " +
            idFields.stream().map(f -> f.atColumn() + " = ?")
                .collect(Collectors.joining(" and "));

        System.out.println("### EXEC QUERY:\n" + query);

        try (var conn = db.open()) {
            var jdbcConn = conn.getJdbcConnection();
            var pstmt = jdbcConn.prepareStatement(query);

            for (var i = 0; i < idFields.size(); i++)
                pstmt.setObject(i + 1, idFields.get(i).accessor().invoke(entity));

            pstmt.execute();

        } catch (SQLException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }

        return null;
    }
}

