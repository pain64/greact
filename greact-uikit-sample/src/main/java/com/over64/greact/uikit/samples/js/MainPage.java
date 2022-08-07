package com.over64.greact.uikit.samples.js;

import com.greact.model.JSExpression;
import com.greact.model.Require;
import com.over64.TypesafeSql;
import com.over64.greact.dom.CodeView;
import com.over64.greact.dom.CodeView.CodeAndView;
import com.over64.greact.dom.HTMLElement;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.uikit.Grid;
import com.over64.greact.uikit.Tab;
import com.over64.greact.uikit.Tabs;
import com.over64.greact.uikit.controls.CheckBox;

import static com.over64.greact.uikit.samples.Main.Server.server;

@Require.CSS("main_page.css")
public class MainPage implements Component0<div> {
    @TypesafeSql.Table("teachers") public record StudyForm(@TypesafeSql.Id int school_id,
                                                           String name, String email, int age) { }
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
                link.href = 'https://i.postimg.cc/TPTs9Bvf/favicon.png';
                document.head.appendChild(link);
                """);
            new div() {{
                className = "context";
                new h1("Greact UIKit Sample");
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
                        src = "https://i.postimg.cc/v8xmyV2J/greact.png";
                        style.marginRight = "15%";
                        onclick = (ev) -> JSExpression.of("window.location.href = '#'");
                    }};
                    new div() {{
                        className = "menu";
                        style.marginLeft = "35%";

                        new a("Text") {{
                            className = "menu-item";
                            href = "#text";
                        }};

                        new a("Images") {{
                            className = "menu-item";
                            href = "#image";
                        }};

                        new a("Buttons") {{
                            className = "menu-item";
                            href = "#button";
                        }};

                        new a("Links") {{
                            className = "menu-item";
                            href = "#link";
                        }};

                        new a("Tables") {{
                            className = "menu-item";
                            href = "#table";
                        }};

                        new a("List") {{
                            className = "menu-item";
                            href = "#list";
                        }};

                        new a("Select") {{
                            className = "menu-item";
                            href = "#select";
                        }};

                        new a("Textarea") {{
                            className = "menu-item";
                            href = "#textarea";
                        }};

                        new a("Input") {{
                            className = "menu-item";
                            href = "#input";
                        }};

                        new a("Checkbox") {{
                            className = "menu-item";
                            href = "#check-box";
                        }};

                        new a("Tabs") {{
                            className = "menu-item";
                            href = "#tabs";
                        }};

                        new a("Grid") {{
                            className = "menu-item";
                            href = "#grid";
                        }};

                        new a("Forms") {{
                            className = "menu-item";
                            href = "#form";
                        }};
                    }};

                }};

                new div() {{
                    className = "examples";

                    new div() {{
                        id = "text";
                        className = "example";

                        new h2("Text usage example") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("EXAMPLE") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new div() {{
                                    new h1("h1 text") {{
                                        style.color = "#703191";
                                    }};
                                }}, renderer());
                        }};
                    }};

                    new div() {{
                        id = "image";
                        className = "example";

                        new h2("Image usage example") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("EXAMPLE") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new img() {{
                                    src = "https://i.postimg.cc/855hkmMh/cat.jpg";
                                    style.maxWidth = "300px";
                                }}, renderer());
                        }};
                    }};

                    new div() {{
                        id = "button";
                        className = "example";

                        new h2("Button usage example") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("EXAMPLE") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new button("Click It!") {{
                                    style.color = "#a675b3";
                                    style.width = "150px";
                                    style.height = "30px";
                                    style.backgroundColor = "#3b2751";
                                }}, renderer());
                        }};
                    }};

                    new div() {{
                        id = "link";
                        className = "example";

                        new h2("Link usage example") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("EXAMPLE") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new a("Link") {{
                                    href = "#";
                                    style.color = "#609123";
                                }}, renderer());
                        }};
                    }};

                    new div() {{
                        id = "table";
                        className = "example";

                        new h2("Table usage example") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("EXAMPLE") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new table() {{
                                    style.border = "1px solid grey";
                                    new tr() {{
                                        new td("Cell 1") {{
                                            style.border = "1px solid grey";
                                        }};
                                        new td("Cell 2") {{
                                            style.border = "1px solid grey";
                                        }};
                                    }};
                                    new tr() {{
                                        new td("Cell 3") {{
                                            style.border = "1px solid grey";
                                        }};
                                        new td("Cell 4") {{
                                            style.border = "1px solid grey";
                                        }};
                                    }};
                                }}, renderer());
                        }};
                    }};

                    new div() {{
                        id = "list";
                        className = "example";

                        new h2("List usage example") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("EXAMPLE") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new ul() {{
                                    style.color = "#6e2f8e";
                                    new li() {{
                                        innerText = "First";
                                    }};
                                    new li() {{
                                        innerText = "Second";
                                    }};
                                }}, renderer());
                        }};
                    }};

                    new div() {{
                        id = "select";
                        className = "example";

                        new h2("Select usage example") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("EXAMPLE") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new select() {{
                                    new option("First") {{
                                        style.color = "#682887";
                                    }};
                                    new option("Second") {{
                                        style.color = "#682887";
                                    }};
                                }}, renderer());
                        }};
                    }};

                    new div() {{
                        id = "textarea";
                        className = "example";

                        new h2("Textarea usage example") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("EXAMPLE") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new textarea() {{
                                    style.backgroundColor = "#fff";
                                    style.border = "3px solid #682887";
                                }}, renderer());
                        }};
                    }};

                    new div() {{
                        id = "input";
                        className = "example";

                        new h2("Input usage example") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("EXAMPLE") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new input() {{
                                    style.border = "3px solid #1240AB";
                                }}, renderer());
                        }};
                    }};

                    new div() {{
                        id = "check-box";
                        className = "example";

                        new h2("CheckBox usage example") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("EXAMPLE") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new div() {{
                                    new CheckBox() {{
                                    }};
                                }}, renderer());
                        }};
                    }};

                    new div() {{
                        id = "tabs";
                        className = "example";

                        new h2("Tabs usage example") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("EXAMPLE") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new Tabs(
                                    new Tab("ex1", new div() {{
                                        new h1("Hello");
                                    }}),
                                    new Tab("ex2", new div() {{
                                        new h1("World");
                                    }})
                                ), renderer());
                        }};
                    }};

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

                    new div() {{
                        id = "form";
                        className = "example";

                        new h2("Form usage example") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("EXAMPLE") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new div() {{
                                    new form() {{
                                        method = "GET";
                                        action = "#";
                                        new input() {{
                                            type = "text";
                                            placeholder = "Enter text...";
                                        }};
                                        new button("Enter") {{
                                            type = "submit";
                                        }};
                                    }};
                                }}, renderer());
                        }};
                    }};
                }};
            }};

            new div() {{
                className = "footer";
                new h3("Created on Greact") {{
                    className = "footer-text";
                    style.color = "#fff";
                    style.textAlign = "center";
                }};
            }};
            JSExpression.of("hljs.highlightAll();" +
                "hljs.initLineNumbersOnLoad();" +
                "document.title='Greact UIKit Sample';");
        }};
    }
}