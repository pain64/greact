package greact.sample.server;

import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

public class TypesafeSql {
    final Sql2o db;

    public TypesafeSql(DataSource ds) {
        db = new Sql2o(ds);
    }

    public <T> List<T> list(String stmt, Class<T> klass, Object... args) {
        try (var conn = db.open()) {
            for (var i = 0; i < args.length; i++)
                stmt = stmt.replace(":" + (i + 1), ":p" + (i + 1));

            var query = conn.createQuery(stmt);

            for (var i = 0; i < args.length; i++)
                query.addParameter("p" + (i + 1), args[i]);

            return query.executeAndFetch(klass);
        }
    }

    public <T> T[] array(String stmt, Class<T> klass, Object... args) {
        return (T[]) list(stmt, klass, args).toArray();
    }

    public <T> T uniqueOrNull(String stmt, Class<T> klass, Object... args) {
        var result = list(stmt, klass, args);
        return result.isEmpty() ? null : result.get(0);
    }
}
