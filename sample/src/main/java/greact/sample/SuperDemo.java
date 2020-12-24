package greact.sample;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import greact.sample.plainjs.demo.User;
import greact.sample.plainjs.demo.UserInfo;
import greact.sample.server.TypesafeSql;
import org.sql2o.Sql2o;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

import static spark.Spark.get;
import static spark.Spark.webSocket;

public class SuperDemo {
    public static void main(String[] args) throws IOException {
        var libraryCode = Loader.libraryCode();

        webSocket("/livereload", new Loader.FileWatcher());
        get("/", (req, res) ->
            """
                <!doctype html>
                <html lang="en">
                  <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">        
                    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/normalize/8.0.1/normalize.css">
                    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css" integrity="sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO" crossorigin="anonymous">
                  </head>
                  <body style="padding:20px;"></body>
                  <script src="/script/lib"></script>
                  <script src="/script/app"></script>
                  <script type="text/javascript">
                    com$over64$greact$dom$Globals.gReactMount(
                      document.body, new greact$sample$plainjs$demo$UsersPage(), [])                      
                          
                    new WebSocket("ws://localhost:4567/livereload")
                      .onmessage = () => location.reload()
                  </script> 
                </html>
                """
        );

        get("/script/lib", (req, res) -> libraryCode);
        get("/script/app", (req, res) -> Loader.appCode());

        var config = new HikariConfig(new Properties() {{
            setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
            setProperty("dataSource.user", "test");
            setProperty("dataSource.password", "test");
            setProperty("dataSource.databaseName", "users");
            put("dataSource.logWriter", new PrintWriter(System.out));
        }});

        var db = new TypesafeSql(new HikariDataSource(config));
        var gson = new Gson();

        get("/users", (req, res) -> {
            res.type("application/json");
            var nameLike = "%" + req.queryParams("nameLike") + "%";

            return db.list("SELECT id, name, age, sex FROM users WHERE name like :1",
                User.class, nameLike);
        }, gson::toJson);

        get("/userInfo", (req, res) -> {
            res.type("application/json");
            var id = Long.valueOf(req.queryParams("id"));

            return db.uniqueOrNull("SELECT faculty, address, phone FROM user_info WHERE user_id = :1",
                UserInfo.class, id);
        }, gson::toJson);
    }
}
