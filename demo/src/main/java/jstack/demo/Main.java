package jstack.demo;

import com.zaxxer.hikari.HikariDataSource;
import jstack.demo.js.MainPage;
import jstack.greact.Loader;
import jstack.greact.rpc.RPC;
import jstack.ssql.SafeSql;
import spark.Spark;

import java.io.IOException;
import java.util.function.Function;

public class Main {
    public static class Server extends RPC<SafeSql> {
        public static <T> T server(Function<SafeSql, T> onServer) {
            throw new RuntimeException("this will be replace with generated code by GReact RPC compiler");
        }
    }

    public static void main(String[] args) throws IOException {

        var ds = new HikariDataSource() {{
            setJdbcUrl("jdbc:postgresql://localhost:5432/jstack_demo");
            setUsername("jstack");
            setPassword("1234");
            setMaximumPoolSize(2);
            setConnectionTimeout(1000);
        }};

        var db = new SafeSql(Dialect.class, ds);
        var server = new Server();

        var bundle = Loader.bundle("", MainPage.class);

        // FIXME: fix websocket timeouts

        Spark.port(3000);
        // Spark.ipAddress("0.0.0.0");
        Spark.staticFiles.location("/resources");

        Spark.get("/*", (req, res) ->
            bundle.handleResource(
                req.pathInfo(), res::status,
                res::type, res.raw().getOutputStream()
            )
        );

        Spark.post(server.rpcUrl, (req, res) ->
            server.handle(db, res::status, res::type, req.raw().getInputStream())
        );

        Spark.init();
    }
}