package com.over64.greact;

import com.over64.greact.dom.HTMLNativeElements.Component0;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
        var module = "";

        if (!livereload) {
            scripts = resources.stream()
                    .filter(res -> res[0].endsWith(".js"))
                    .map(res -> " <script src=\""
                            + res[0] + (res.length == 2 ? "?hash=" + res[1] : "") + "\"></script>")
                    .collect(Collectors.joining("\n", "", "\n"));
        } else {
            scripts = resources.stream()
                    .filter(res -> (res[0].endsWith(".js") && (res[0].startsWith("greact") || res[0].startsWith("std-"))))
                    .map(res -> " <script src=\"" + res[0] + "\"" + "></script>")
                    .collect(Collectors.joining("\n", "", "\n"));

            module = resources.stream()
                    .filter(res -> (res[0].endsWith(".js") && !(res[0].startsWith("greact") || res[0].startsWith("std-"))))
                    .map(res -> " <script src=\"" + res[0] + "\"" + "></script>")
                    .collect(Collectors.joining("\n", "", "\n"));

            try {
                var bundleJs = Path.of(Objects.requireNonNull(Loader.class.getResource("/bundle/")).toURI()).resolve("bundle.js").toFile();
                if (bundleJs.exists()) {
                    bundleJs.delete();
                }
                bundleJs.createNewFile(); // import A from './modA.js?t=1231232312'

                var data = resources.stream()
                        .filter(res -> (res[0].endsWith(".js") && !(res[0].startsWith("greact") || res[0].startsWith("std-"))))
                        .map(res -> "import " + res[0].substring(0, res[0].length() - 3).replace('.', '_') + " from '/" + res[0] + "?t=" + new Date().getTime() + "'")
                        .collect(Collectors.joining("\n", "", "\n"));

                Files.writeString(bundleJs.toPath(), data + """
                                                
                        var handler;
                        export function start() {
                            handler = setInterval(() => {
                                console.log('greet from bundle!!');
                                // Global.greet();
                                // new A().greet();
                                // new B().greet();
                            }, 1000);
                        }
                                                
                        export function stop() {
                            clearInterval(handler)
                        }
                        """);

                var moduleScripts = resources.stream()
                        .filter(res -> (res[0].endsWith(".js") && !(res[0].startsWith("greact") || res[0].startsWith("std-")))).toList();

                for (String[] script : moduleScripts) {
                    var file = Path.of(Objects.requireNonNull(Loader.class.getResource("/bundle/")).toURI()).resolve(script[0]);
                    Files.writeString(file, "export default " + Files.readString(file));

                    for (String[] script2 : moduleScripts) {
                        if (!script[0].equals(script2[0])) {

                        }
                    }
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
                throw new IOException("Can't create bundle.js file");
            }

        }
        var mount = "<script type=\"text/javascript\">\n" +
                "com_over64_greact_dom_GReact._mmount(document.body, new " +
                entry.getName().replace(".", "_") + ", [])" +
                "</script>";

        var reloadWS = """
                <script>   
                function reloadCss()
                {
                   var links = document.getElementsByTagName("link");
                   for (var cl in links)
                   {
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
                                
                var mod;
                async function loadApp() {
                        if(mod != null) mod.stop();
                        mod = await import('./bundle.js?t=' + new Date().getTime())
                        mod.start();
                }
                 
                 function reloadJs() {
                     loadApp();
                 }
                                
                  let ws = new WebSocket("ws://localhost:8080/greact_livereload_events")
                  ws.onmessage = function(event) {
                           if (event.data === "reload") {
                                document.location.reload();
                           } else if(event.data === "update"){
                                reloadCss();
                                reloadJs();
                           }
                  };
                  setInterval(() => ws.send('heartbeat'), 1000 * 60);
                  reloadJs();
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
