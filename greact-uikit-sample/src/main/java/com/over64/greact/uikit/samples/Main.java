package com.over64.greact.uikit.samples;

import com.over64.TypesafeSql;
import com.over64.greact.Loader;
import com.over64.greact.rpc.RPC;
import com.over64.greact.uikit.samples.js.MainPage;
import com.zaxxer.hikari.HikariDataSource;
import spark.Spark;

import java.io.IOException;
import java.util.function.Function;

public class Main {
    static final String RPC_BASE_URL = "/rpc";

    public static class Server extends RPC<TypesafeSql> {
        Server() {super("com.over64.greact.uikit.samples.js");}
        @RPCEntryPoint(RPC_BASE_URL)
        public static <T> T server(Function<TypesafeSql, T> onServer) {
            throw new RuntimeException("this will be replace with generated code by GReact RPC compiler");
        }
    }

    public static void main(String[] args) throws IOException {

        var ds = new HikariDataSource() {{
            setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
            setUsername("postgres");
            setPassword("postgres");
            setMaximumPoolSize(2);
            setConnectionTimeout(1000);
        }};

        var db = new TypesafeSql(ds);
        var server = new Server();

        var resources = Loader.bundle(MainPage.class);

        // FIXME: fix websocket timeouts

        Spark.port(3000);
        // Spark.ipAddress("0.0.0.0");
        Spark.staticFiles.location("/resources");
        Spark.get("/*", (req, res) -> {
            var resourceName = req.pathInfo();
            var found = resources.get(resourceName);
            if (found != null) {
                if (resourceName.endsWith(".css"))
                    res.type("text/css");
                if (resourceName.endsWith(".js"))
                    res.type("text/javascript");
                return found.get();
            } else {
                res.status(404);
                return "not found";
            }
        });

        Spark.post(RPC_BASE_URL, (req, res) -> {
            res.status(200);
            res.type("application/json");
            try {
                return server.handle(db, req.raw().getReader());
            } catch (Exception ex) {
                ex.printStackTrace();
                res.status(500);
                var msg = ex.getMessage();
                if (msg == null) // FIXME: not so good
                    msg = ex.getCause().getMessage();
                return RPC.rpcErrorJson(msg);
            }
        });

        Spark.init();
    }
}