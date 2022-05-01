package com.over64.greact;

import com.over64.greact.dom.HTMLNativeElements.Component0;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Loader {
    public static Map<String, Supplier<String>> bundle(Class<? extends Component0<?>> entry) throws IOException {
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
            "com_over64_greact_dom_GReact._mmount(document.body, new " +
            entry.getName().replace(".", "_") + ", [])" +
            "</script>";

        var reloadWS = """
                <script>
                function reloadCss(filename, hash)
                 {
                     var links = document.getElementsByTagName("link");
                     for (var cl in links)
                     {
                         var link = links[cl];
                         if(link.rel === "stylesheet") {
                             temp = link.href.split("/");
                             file = temp[temp.length - 1];
                             if(file.startsWith(filename + "?hash=")) {
                                 file = filename + "?hash=" + hash;
                                 link.href = file;
                             }
                         }
                     }
                 }
                 
                 function reloadJs(filename, hash) {
                     Array.from(document.getElementsByTagName("script")).map(i => {
                         temp = i.src.split("/");
                         file = temp[temp.length - 1];
                         if(file.startsWith(filename + "?hash=")) {
                             i.src = filename + "?hash=" + hash;
                         }
                     })
                 }
                
                  let ws = new WebSocket("ws://localhost:8080/greact_livereload_events")
                  ws.onmessage = function(event) {
                           if (event.data === "reload") {
                                document.location.reload();
                           } else {
                                arr = event.data.split("\\n");
                                for (i = 0; i < arr.length; i+=1) {
                                    if (arr[i].split(" ")[0].endsWith("css")) {
                                        reloadCss(arr[i].split(" ")[0], arr[i].split(" ")[1]);
                                    } else if (arr[i].split(" ")[0].endsWith("js")) {
                                        reloadJs(arr[i].split(" ")[0], arr[i].split(" ")[1]);
                                    }
                                }
                           }
                  };
                  setInterval(() => ws.send('heartbeat'), 1000 * 60);
                </script>""";

        if (!livereload) reloadWS = "";

        var page = String.format("""
            <!doctype html>
            <html lang="en">
              <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
                %s
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
