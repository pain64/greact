package com.over64.greact.uikit.samples.js;

import com.greact.model.JSExpression;
import com.greact.model.Require;
import com.over64.TypesafeSql;
import com.over64.greact.dom.CodeView;
import com.over64.greact.dom.CodeView.CodeAndView;
import com.over64.greact.dom.HTMLElement;
import com.over64.greact.dom.HTMLNativeElements;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.uikit.Array;
import com.over64.greact.uikit.Grid;
import com.over64.greact.uikit.Tab;
import com.over64.greact.uikit.Tabs;
import com.over64.greact.uikit.controls.CheckBox;

@Require.CSS("main_page.css")
public class MainPage implements Component0<div> {
    @TypesafeSql.Table("teachers") public record StudyForm(@TypesafeSql.Id int school_id,
                                                           String name, String email, int age) { }
    public record Data(int id, String text) { }
    private <T extends HTMLElement> Component1<div, CodeAndView<T>> rendererWithHeight(int height) {
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
                        var nl = "\n";
                        innerHTML = JSExpression.of("'&#10' + codeAndView.code.replaceAll(nl, '&#10').replaceAll(' ' , '&nbsp') + '&#10'");
                        className = "language-java";
                        style.height = height + "px";
                    }};
                }};
            }};
    }

    @Override
    public div mount() {
        return new div() {{
            new div() {{
                className = "context";
                new h1("Документация Greact");
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
                        src = "https://i.postimg.cc/v8xmyV2J/greact.png";
                        style.marginRight = "15%";
                    }};

                    new div() {{
                        className = "menu";
                        style.marginLeft = "35%";

                        new a("Текст") {{
                            className = "menu-item";
                            href = "#text";
                        }};

                        new a("Картинки") {{
                            className = "menu-item";
                            href = "#image";
                        }};

                        new a("Кнопки") {{
                            className = "menu-item";
                            href = "#button";
                        }};

                        new a("Ссылки") {{
                            className = "menu-item";
                            href = "#link";
                        }};

                        new a("Таблицы") {{
                            className = "menu-item";
                            href = "#table";
                        }};

                        new a("Обычний список") {{
                            className = "menu-item";
                            href = "#list";
                        }};

                        new a("Всплывающий список") {{
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

                        new a("Check box") {{
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

                        new a("Формы") {{
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

                        new h2("Примеры использования текста") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("ПРИМЕР") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new div() {{
                                    new h1("h1 text") {{
                                        style.color = "#703191";
                                    }};
                                    new h3("h3 text") {{
                                        style.color = "#44944A";
                                    }};
                                    new h5("h5 text") {{
                                        style.color = "#4285B4";
                                    }};
                                }}, rendererWithHeight(260));
                        }};
                    }};

                    new div() {{
                        id = "image";
                        className = "example";

                        new h2("Пример использования изображений") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("ПРИМЕР") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new img() {{
                                    src = "https://clck.ru/YFWWu";
                                }}, rendererWithHeight(120));
                        }};
                    }};

                    new div() {{
                        id = "button";
                        className = "example";

                        new h2("Пример использования кнопок") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("ПРИМЕР") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new button("Click It!") {{
                                    style.color = "#a675b3";
                                    style.width = "150px";
                                    style.height = "30px";
                                    style.backgroundColor = "#3b2751";
                                }}, rendererWithHeight(180));
                        }};
                    }};

                    new div() {{
                        id = "link";
                        className = "example";

                        new h2("Пример использования ссылок") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("ПРИМЕР") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new a("Link") {{
                                    href = "#";
                                    style.color = "#609123";
                                }}, rendererWithHeight(150));
                        }};
                    }};

                    new div() {{
                        id = "table";
                        className = "example";

                        new h2("Пример использования таблиц") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("ПРИМЕР") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new table() {{
                                    style.border = "1px solid grey";
                                    new tr() {{
                                        new td("Ячейка 1") {{
                                            style.border = "1px solid grey";
                                        }};
                                        new td("Ячейка 2") {{
                                            style.border = "1px solid grey";
                                        }};
                                    }};
                                    new tr() {{
                                        new td("Ячейка 3") {{
                                            style.border = "1px solid grey";
                                        }};
                                        new td("Ячейка 4") {{
                                            style.border = "1px solid grey";
                                        }};
                                    }};
                                }}, rendererWithHeight(400));
                        }};
                    }};

                    new div() {{
                        id = "list";
                        className = "example";

                        new h2("Пример использования списка") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("ПРИМЕР") {{
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
                                }}, rendererWithHeight(220));
                        }};
                    }};

                    new div() {{
                        id = "select";
                        className = "example";

                        new h2("Пример использования всплывающего списка") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("ПРИМЕР") {{
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
                                }}, rendererWithHeight(200));
                        }};
                    }};

                    new div() {{
                        id = "textarea";
                        className = "example";

                        new h2("Пример использования textarea") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("ПРИМЕР") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new textarea() {{
                                    style.backgroundColor = "#fff";
                                    style.border = "3px solid #682887";
                                }}, rendererWithHeight(150));
                        }};
                    }};

                    new div() {{
                        id = "input";
                        className = "example";

                        new h2("Пример использования input") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("ПРИМЕР") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new input() {{
                                    style.border = "3px solid #1240AB";
                                }}, rendererWithHeight(120));
                        }};
                    }};

                    new div() {{
                        id = "check-box";
                        className = "example";

                        new h2("Пример использования CheckBox") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("ПРИМЕР") {{
                                className = "ex-text";
                            }};

                            new CodeView<>(() ->
                                new div() {{
                                    new CheckBox() {{
                                    }};
                                }}, rendererWithHeight(150));
                        }};
                    }};

                    new div() {{
                        id = "tabs";
                        className = "example";

                        new h2("Пример использования tabs") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("ПРИМЕР") {{
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
                                ), rendererWithHeight(200));
                        }};
                    }};

                    new div() {{
                        id = "grid";
                        className = "example";

                        new h2("Пример использования grid") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("ПРИМЕР") {{
                                className = "ex-text";
                            }};

//                                    server(db -> db.updateSelf(new StudyForm(3, "abbalb", "ddd@dd.er", 25)));
//                                    var data = server(db -> db.select(StudyForm.class));

                            new CodeView<>(() ->
                                new div() {{
                                    var data = Array.of(
                                        new Data(1, "Some text 1"),
                                        new Data(2, "Some text 2")
                                    );
                                    new Grid<>(data) {{
                                        adjust(Data::id);
                                        onRowChange = row -> JSExpression.of("console.log(row)");
                                    }};
                                }}, rendererWithHeight(240));
                        }};
                    }};

                    new div() {{
                        id = "form";
                        className = "example";

                        new h2("Пример использования формы") {{
                            className = "heading";
                        }};

                        new div() {{
                            className = "vision-code";

                            new h3("ПРИМЕР") {{
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
                                }}, rendererWithHeight(290));
                        }};
                    }};
                }};
            }};

            new div() {{
                className = "footer";
                new h3("Создано на Greact") {{
                    className = "footer-text";
                    style.color = "#fff";
                    style.textAlign = "center";
                }};
            }};
            JSExpression.of("hljs.highlightAll();");
        }};
    }
}