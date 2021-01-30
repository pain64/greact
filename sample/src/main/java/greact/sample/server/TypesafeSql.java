package greact.sample.server;

import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class TypesafeSql {
    final Sql2o db;

    public TypesafeSql(DataSource ds) {
        db = new Sql2o(ds);
    }

    Query queryWithParams(Connection conn, String stmt, Object... args) {
        for (var i = 0; i < args.length; i++)
            stmt = stmt.replace(":" + (i + 1), ":p" + (i + 1));

        var query = conn.createQuery(stmt);

        for (var i = 0; i < args.length; i++)
            query.addParameter("p" + (i + 1), args[i]);

        return query;
    }

    public <T> List<T> list(String stmt, Class<T> klass, Object... args) {
        try (var conn = db.open()) {
            var query = queryWithParams(conn, stmt, args);

            if (klass.isRecord()) {
                var data = query.executeAndFetch(new ResultSetHandler<T>() {
                    @Override
                    public T handle(ResultSet rs) throws SQLException {
                        var constuctor = klass.getDeclaredConstructors()[0];
                        constuctor.setAccessible(true);
                        var consArgs = new Object[constuctor.getParameters().length];
                        for (var i = 0; i < consArgs.length; i++) {
                            var param = constuctor.getParameters()[i];
                            consArgs[i] = rs.getObject(param.getName());
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
            queryWithParams(conn, stmt, args).executeUpdate();
            return null;
        }
    }
}
