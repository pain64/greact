package jstack.greact.uikit.docs;

import jstack.greact.Loader;
import jstack.greact.uikit.docs.js.MainPage;
import spark.Spark;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        var resources = Loader.bundle("", MainPage.class);

        Spark.port(3000);
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

        Spark.init();
    }
}