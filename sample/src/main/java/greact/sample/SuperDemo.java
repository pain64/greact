package greact.sample;

import com.over64.greact.rpc.RPC;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import greact.sample.server.TypesafeSql;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.function.Function;

import static spark.Spark.*;

public class SuperDemo {
    static final String RPC_BASE_URL = "/rpc";
    public static class Server extends RPC<TypesafeSql> {
        @RPCEntryPoint(RPC_BASE_URL)
        public static <T> T server(Function<TypesafeSql, T> onServer) {
            throw new RuntimeException("this will be replace with generated code by GReact RPC compiler");
        }
    }

    public static void main(String[] args) throws IOException {
        // FIXME: reconfigure DB for oracle
        var config = new HikariConfig(new Properties() {{
            setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
            setProperty("dataSource.user", "test");
            setProperty("dataSource.password", "test");
            setProperty("dataSource.databaseName", "users");
            put("dataSource.logWriter", new PrintWriter(System.out));
        }});

        var db = new TypesafeSql(new HikariDataSource(config));
        //TypesafeSql db = null;
        var server = new Server();

        var libraryCode = Loader.libraryCode();
        webSocket("/livereload", new Loader.FileWatcher());
        post(RPC_BASE_URL, (req, res) -> {
            res.status(200);
            res.type("application/json");
            return server.handle(db, req.raw().getReader());
        });

//        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/normalize/8.0.1/normalize.css">
//        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css" integrity="sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO" crossorigin="anonymous">

        get("/", (req, res) ->
            """
                <!doctype html>
                <html lang="en">
                  <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
                    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/normalize/8.0.1/normalize.css">
                    <script src="https://cdn.jsdelivr.net/npm/feather-icons/dist/feather.min.js"></script>
                    <style>
                      html {
                        box-sizing: border-box;
                      }
                      *, *:before, *:after {
                        box-sizing: inherit;
                      }
                      body {
                        font-family: -apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial,sans-serif,"Apple Color Emoji","Segoe UI Emoji","Segoe UI Symbol","Noto Color Emoji";
                        font-weight:400;
                        color: #403d3d;
                      }
                    </style>
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

        get("/style", (req, res) -> {
            res.type("text/css");
            return SuperDemo.class.getResourceAsStream("/style.css").readAllBytes();
        });
        get("/script/lib", (req, res) -> libraryCode);
        get("/script/app", (req, res) -> Loader.appCode());
        init();
    }
}
