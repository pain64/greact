package jstack.greact.uikit.docs;

import jstack.greact.Loader;
import jstack.greact.uikit.docs.js.MainPage;
import spark.Spark;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        var bundle = Loader.bundle("", MainPage.class);

        Spark.port(3000);
        Spark.staticFiles.location("/resources");

        Spark.get("/*", (req, res) ->
            bundle.handleResource(
                req.pathInfo(), res::status,
                res::type, res.raw().getOutputStream()
            )
        );

        Spark.init();
    }
}