package com.over64.greact;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Loader {
    public static Map<String, Supplier<String>> bundle(Class<?> entry) throws IOException {
        var bundleFile = Objects.requireNonNull(Loader.class.getResourceAsStream("/bundle/.bundle"));
        var bundle = new String(bundleFile.readAllBytes());
        var filesWithCode = bundle.split("\n");

        var livereload = bundle.endsWith("livereload\n");

        var resources = Arrays.stream(filesWithCode)
            .map(res -> res.split(" ")).toList();

        var styles = resources.stream()
            .filter(res -> res[0].endsWith(".css"))
            .map(res -> " <link rel=\"stylesheet\" href=\""
                + res[0] + (res.length == 2 ? "?hash=" + res[1] : "") + "\">")
            .collect(Collectors.joining("\n", "", "\n"));

        var scripts = resources.stream()
            .filter(res -> res[0].endsWith(".js"))
            .map(res -> " <script src=\""
                + res[0] + (res.length == 2 ? "?hash=" + res[1] : "") + "\"></script>")
            .collect(Collectors.joining("\n", "", "\n"));

        var mount = "<script type=\"text/javascript\">\n" +
            "com$over64$greact$dom$GReact._mmount(document.body, new " +
            entry.getName().replace(".", "$") + ", [])" +
            "</script>";

        var reloadWS = """
            <script>
              let ws = new WebSocket("ws://localhost:8080/greact_livereload_events")
              ws.onmessage = m => document.location.reload();
              setInterval(() => ws.send('heartbeat'), 1000 * 60);
            </script>""";

        if (!livereload) reloadWS = "";

        var page = String.format("""
            <!doctype html>
            <html lang="en">
              <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/normalize/8.0.1/normalize.css">
                %s
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
              <body></body>
              %s
              %s
              %s
              </html>""", styles, scripts, mount, reloadWS);

        var all = new HashMap<String, Supplier<String>>();
        all.put("/", () -> page);

        for (var res : resources) {
            all.put("/" + res[0], () -> {
                try {
                    return new String(Loader.class.getResourceAsStream("/bundle/" + res[0]).readAllBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return all;
    }
}
