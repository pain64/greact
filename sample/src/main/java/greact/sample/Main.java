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
                    <div id="view00"></div>
                    <hr>
                    <div id="view01"></div>
                    <hr>
                    <div id="view02"></div>
                    <hr>
                    <div id="view03">BROKEN DEMO</div>
                    <hr>
                    <div id="view04"></div>
                  </body>
                   <script src="/script/lib"></script>
                   <script src="/script/app"></script>
                   <script type="text/javascript">
                        new greact$sample$plainjs$_00HelloWorld()
                          .mount(document.getElementById("view00"))
                          
                        new greact$sample$plainjs$_01IfStmt()
                          .mount(document.getElementById("view01"))
                        
                        new greact$sample$plainjs$_02IfElseStmt()
                          .mount(document.getElementById("view02"))
                          
                        
                          
                        new greact$sample$plainjs$_04DependsOn()
                          .mount(document.getElementById("view04"))
                          
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
