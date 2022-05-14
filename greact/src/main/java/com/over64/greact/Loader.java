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

        var scripts = "";
        var mount = "<script type=\"text/javascript\">\nfunction mount(){\n" +
            "com_over64_greact_dom_GReact._mmount(document.body, new " +
            entry.getName().replace(".", "_") + ", [])" +
            "\n}\nmount()\n</script>";

        if (!livereload) {
            scripts = resources.stream()
                .filter(res -> res[0].endsWith(".js"))
                .map(res -> " <script src=\""
                    + res[0] + (res.length == 2 ? "?hash=" + res[1] : "") + "\"></script>")
                .collect(Collectors.joining("\n", "", "\n"));
        } else {
            scripts = resources.stream()
                .filter(res -> (res[0].endsWith(".js")))
                .map(res -> " <script src=\"" + res[0] + "\"" + "></script>")
                .collect(Collectors.joining("\n", "", "\n"));
        }

        var reloadWS = """
            <script>
              function reloadCss() {
                var links = document.getElementsByTagName("link");
                for (var cl in links) {
                  var link = links[cl];
                  if(link.rel === "stylesheet") {
                    temp = link.href.split("/");
                    file = temp[temp.length - 1].split("?t=")[0];
                    if(file.includes(".css")) {
                      link.href = file + "?t=" + Date.now();
                    }
                  }
                }
              }

              function reloadJs(changeFiles) {
                for (i = 0; i < changeFiles.length; i++) {
                  var data = changeFiles[i].split("**_SYMB_**");
                  var class_ = data[0];
                  var code = data[1];
                  eval(class_ + " = " + code);
                }
                document.body.innerHTML = '';
                mount();
              }

              let ws = new WebSocket("ws://localhost:8080/greact_livereload_events")
              ws.onmessage = function(event) {
                if (event.data === "reload") {
                  document.location.reload();
                } else if(event.data.startsWith("update")) {
                  var changeFiles = [];
                  if (event.data.length > 7) {
                    changeFiles = event.data.substring(18).split("**_GREACT_**");
                  }
                  reloadCss();
                  reloadJs(changeFiles);
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
              </html>""", styles, scripts, reloadWS, mount);

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

        if (livereload) {
            all.put("/" + "bundle.js", () -> {
                try {
                    return new String(Loader.class.getResourceAsStream("/bundle/" + "bundle.js").readAllBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return all;
    }
}
