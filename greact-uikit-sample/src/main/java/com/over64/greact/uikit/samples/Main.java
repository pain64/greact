package com.over64.greact.uikit.samples;

import com.over64.greact.Loader;
import com.over64.greact.uikit.samples.js.MainPage;
import spark.Spark;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        var resources = Loader.bundle(MainPage.class);

        Spark.port(3000);

        Spark.get("/*", (req, res) -> {
            var resourceName = req.pathInfo();
            var found = resources.get(resourceName);
            if (found != null) {
                if (resourceName.endsWith(".css"))
                    res.type("text/css");
                return found.get();
            } else {
                res.status(404);
                return "not found";
            }
        });
    }
}
