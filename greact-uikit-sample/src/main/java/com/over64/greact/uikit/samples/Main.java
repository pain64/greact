package com.over64.greact.uikit.samples;

import com.over64.greact.Loader;
import com.over64.greact.uikit.samples.js.MainPage;
import com.over64.greact.rpc.RPC;
import com.zaxxer.hikari.HikariDataSource;

import com.over64.TypesafeSql;
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
        var env = System.getenv();
        var serverPort = Integer.parseInt(env.getOrDefault("IIAS_APP_PORT", "4567"));

        var ds = new HikariDataSource() {{
            setDataSourceClassName("oracle.jdbc.pool.OracleDataSource");
            addDataSourceProperty("url", "jdbc:oracle:thin:@//[192.168.11.36][1521]/student");
            setUsername("iias_owner");
            setPassword("i2000");
            setMaximumPoolSize(2);
            setConnectionTimeout(1000);
            setConnectionTestQuery("select 1 from dual");
        }};

        var db = new TypesafeSql(ds);
        var server = new Server();

        var resources = Loader.bundle(MainPage.class);

        // FIXME: fix websocket timeouts

        Spark.port(serverPort);
        // Spark.ipAddress("0.0.0.0");
        Spark.staticFiles.location("/resources");
        Spark.get("/*", (req, res) -> {
            var resourceName = req.pathInfo();
            var found = resources.get(resourceName);
            if(found != null) {
                if(resourceName.endsWith(".css"))
                    res.type("text/css");
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