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

        var mount = "<script type=\"text/javascript\">\nfunction mount(){\n" +
            "com_over64_greact_dom_GReact._mmount(document.body, new window." +
            entry.getName().replace(".", "_") + ", [])" +
            "\n}\nmount()\n</script>";

        var scripts = livereload ?
            resources.stream()
                .filter(res -> (res[0].endsWith(".js")))
                .map(res -> " <script src=\"" + res[0] + "\"" + "></script>")
                .collect(Collectors.joining("\n", "", "\n")) :
            resources.stream()
                .filter(res -> res[0].endsWith(".js"))
                .map(res -> " <script src=\""
                    + res[0] + (res.length == 2 ? "?hash=" + res[1] : "") + "\"></script>")
                .collect(Collectors.joining("\n", "", "\n"));

        var reloadWS = livereload ? """
            <script>
              function reloadCss(filename) {
                var links = document.getElementsByTagName("link");
                for (var cl in links) {
                  var link = links[cl];
                  if(link.rel === "stylesheet") {
                    var temp = link.href.split("/");
                    var file = temp[temp.length - 1].split("?t=")[0];
                    if(file === filename) {
                      link.href = file + "?t=" + Date.now();
                    }
                  }
                }
              }
              
              function reloadJs(filename) {
                  var scripts = document.getElementsByTagName("script");
                      
                  for (var script of scripts) {
                     
                       var temp = script.src.split("/")
                       var src = temp[temp.length - 1].split("?t=")[0];
                       if(src === filename) {
                            script.parentNode.removeChild(script);

                            var newScript = document.createElement('script');
                            var localSrc = src + "?t=" + Date.now();
                            newScript.src = localSrc;
                            document.head.appendChild(newScript);
                                                       
                            if (window.XMLHttpRequest)
                            {
                                xmlhttp=new XMLHttpRequest();
                            }
                            else
                            {
                                xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
                            }
                            xmlhttp.open("GET",localSrc,false);
                            xmlhttp.send();
                            
                            eval(xmlhttp.responseText);
                       }
                  }
              }

              let ws = new WebSocket("ws://localhost:8080/greact_livereload_events")
              ws.onmessage = function(event) {
                if (event.data === "reload") {
                  document.location.reload();
                } else if(event.data.startsWith("update")) {
                var reload = false;
                  for(var file of event.data.substring(7).split("$")) {
                        if (file.endsWith(".js")) {
                            reloadJs(file);
                            reload = true;
                        } else if (file.endsWith(".css")) {
                            reloadCss(file);
                        }
                  }
                  if (reload) {
                    document.body.innerHTML = '';
                    mount();
                  }
                }
              };
              setInterval(() => ws.send('heartbeat'), 1000 * 60);
            </script>""" : "";

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
