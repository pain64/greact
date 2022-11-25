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

        var isHotReload = bundle.endsWith("\nhot-reload");

        var resources = Arrays.stream(filesWithCode)
            .map(res -> res.split(" ")).toList();

        var styles = resources.stream()
            .filter(res -> res[0].endsWith(".css"))
            .map(res -> " <link rel=\"stylesheet\" href=\""
                + res[0] + (res.length == 2 ? "?hash=" + res[1] : "") + "\">")
            .collect(Collectors.joining("\n", "", "\n"));

        var mount = """
            <script type="text/javascript">
                const appRoot = document.body.appendChild(document.createElement('div'));
                function mount() {
                    appRoot.innerHTML = '';
                    com_over64_greact_dom_GReact._mmount(appRoot, new %s(), []);
                }
                mount();
            </script>""".formatted(entry.getName().replace(".", "_"));

        var reloadWS = isHotReload ? """
            <script>
            function reloadCss(filename) {
                var links = document.getElementsByTagName("link");
               for(let link of links) {
                    if (link.rel === "stylesheet") {
                        var temp = link.href.split("/");
                        var file = temp[temp.length - 1].split("?t=")[0];
                        if (file === filename) {
                            link.href = file + "?t=" + Date.now();
                        }
                    }
                }
            }
                        
            async function reloadJs(filename) {
                return new Promise(function(resolve, reject) {
                    const scripts = document.getElementsByTagName("script");
                    for (let script of scripts) {
                        const temp = script.src.split("/")
                        const src = temp[temp.length - 1].split("?t=")[0];
                        
                        if (src === filename) {
                            script.parentNode.removeChild(script);
                            const newScript = document.createElement('script');
                            newScript.src = script.src;
                            newScript.onload = () => {
                                resolve();
                            }
                            document.body.appendChild(newScript);
                        }
                    }
                })
            }
                        
                        
            const ws = new WebSocket("ws://localhost:8080/greact_reload_events")
            ws.onmessage = function(event) {
                if (event.data === "reload") {
                    document.location.reload();
                } else if (event.data.startsWith("update")) {
                    let awaitJsLoad = false;
                    var jsPromises = [];
                    for (let file of event.data.substring(7).split("\\n")) {
                        if (file.endsWith(".js")) {
                            jsPromises.push(reloadJs(file));
                            awaitJsLoad = true;
                        } else if (file.endsWith(".css")) {
                            reloadCss(file);
                        }
                    }
                    if (awaitJsLoad) {
                        (async () => {
                            await Promise.all(jsPromises);
                            mount();
                        })();
                    }
                }
            };
            setInterval(() => ws.send('heartbeat'), 1000 * 60);
            </script>""" : "";
        var scripts = isHotReload ?
            resources.stream()
                .filter(res -> (res[0].endsWith(".js")))
                .map(res -> " <script src=\"" + res[0] + "\"" + "></script>")
                .collect(Collectors.joining("\n", "", "\n")) :
            resources.stream()
                .filter(res -> res[0].endsWith(".js"))
                .map(res -> " <script src=\""
                    + res[0] + (res.length == 2 ? "?hash=" + res[1] : "") + "\"></script>")
                .collect(Collectors.joining("\n", "", "\n"));


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
