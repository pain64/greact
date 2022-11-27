package jstack.demo.js;

import jstack.greact.dom.HTMLNativeElements.*;
import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.Require;
import jstack.tsql.TypesafeSql;
import jstack.greact.dom.CodeView;
import jstack.greact.dom.CodeView.CodeAndView;
import jstack.greact.dom.HTMLElement;
import jstack.greact.uikit.Grid;
import static jstack.demo.Main.Server.server;

@Require.CSS("main_page.css")
public class MainPage implements Component0<div> {
    @TypesafeSql.Table("teachers") public record StudyForm(@TypesafeSql.Id long school_id, String name, String email, int age) {}
    private <T extends HTMLElement> Component1<div, CodeAndView<T>> renderer() {
        return codeAndView ->
            new div() {{
                new div() {{
                    className = "view";
                    new slot<>(codeAndView.view);
                }};

                new div() {{
                    className = "line";
                }};

                new pre() {{
                    new code() {{
                        style.borderRadius = "5px";
                        style.backgroundColor = "#fff";
                        var nl = "\n";
                        innerHTML = JSExpression.of("codeAndView.code.replaceAll(nl, '&#10').replaceAll(' ' , '&nbsp')");
                        className = "language-java";
                    }};
                }};
            }};
    }

    @Override
    public div mount() {
        return new div() {{
            JSExpression.of("""
                var list = document.querySelectorAll('link[rel=\"icon\"], link[rel=\"shortcut icon\"]');
                list.forEach(function(element) {
                    element.parentNode.removeChild(element);
                });
                                
                var link = document.createElement('link');
                link.rel = 'icon';
                link.href = 'https://i.postimg.cc/Kcs7MhxX/favicon.png';
                document.head.appendChild(link);
                """);
            new div() {{
                className = "context";
                new h1("Greact UIKit Demo");
            }};

            new div() {{
                className = "area";
                new ul() {{
                    className = "circles";
                    new li();
                    new li();
                    new li();
                    new li();
                    new li();
                    new li();
                    new li();
                    new li();
                    new li();
                    new li();
                }};
            }};

            new div() {{
                className = "main";

                new div() {{
                    className = "menu";
                    new img() {{
                        style.cursor = "pointer";
                        src = "https://i.postimg.cc/rwdBBqZJ/logo.png";
                        style.marginRight = "15%";
                        onclick = (ev) -> JSExpression.of("window.location.href = '#'");
                    }};
                    new div() {{
                        className = "menu";
                        style.marginLeft = "35%";
                        new a("Grid") {{
                            className = "menu-item";
                            href = "#grid";
                        }};
                    }};

                }};

                new div() {{
                    className = "examples";
                    new div() {{
                        id = "grid";
                        className = "example";

                        new h2("Grid usage example") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("EXAMPLE") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new div() {{
                                    var data = server(db -> db.select(StudyForm.class));
                                    new Grid<>(data) {{
                                        adjust(StudyForm::school_id);
                                        onRowChange = row -> JSExpression.of("console.log(row)");
                                    }};
                                }}, renderer());
                        }};
                    }};
                }};
            }};
            JSExpression.of("hljs.highlightAll();" +
                "hljs.initLineNumbersOnLoad();" +
                "document.title='Greact UIKit Demo';");
        }};
    }
}