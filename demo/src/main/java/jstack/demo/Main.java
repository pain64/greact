package jstack.demo;

import jstack.greact.Loader;
import jstack.greact.rpc.RPC;
import jstack.demo.js.MainPage;

import com.zaxxer.hikari.HikariDataSource;
import jstack.ssql.SafeSql;
import spark.Spark;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
    static final String RPC_BASE_URL = "/rpc";

    public static class Server extends RPC<SafeSql> {
        Server() { super("jstack.demo.js"); }
        @RPCEntryPoint(RPC_BASE_URL)
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

        var resources = Loader.bundle("", MainPage.class);

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
                return RPC.rpcErrorJson(msg, Arrays.stream(ex.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n")));
            }
        });

        Spark.init();
    }
}