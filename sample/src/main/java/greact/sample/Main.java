package greact.sample;

import static spark.Spark.*;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        var libraryCode = Loader.libraryCode();

        webSocket("/livereload", new Loader.FileWatcher());
        get("/", (req, res) ->
            """
                <!doctype html>
                <html lang="en">
                  <head>
                    <!-- Required meta tags -->
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
                                
                    <!-- Bootstrap CSS -->
                    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/normalize/8.0.1/normalize.css">
                    <title>Hello, jScripter!</title>
                  </head>
                  <body>
                    <div id="view"></div>
                  </body>
                   <script src="/script/lib"></script>
                   <script src="/script/app"></script>
                   <script type="text/javascript">
                        new greact$sample$plainjs$HW()
                          .mount(document.getElementById("view"))
                        new WebSocket("ws://localhost:4567/livereload")
                            .onmessage = () => location.reload()
                   </script>
                   
                </html>
                """
        );

        get("/script/lib", (req, res) -> libraryCode);
        get("/script/app", (req, res) -> Loader.appCode());
    }
}
