package com.over64.greact.uikit.samples.js;

import com.greact.model.CSS.Require;
import com.over64.greact.dom.CodeView;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.uikit.*;

@Require("main_page.css")
public class MainPage implements Component0<div> {
    private Component1<div, CodeView.CodeAndView> rendererWithHeight(int height) {
        return codeAndView ->
                new div() {{
                    new div() {{
                        className = "view";
                        new slot<>(codeAndView.view);
                    }};

                    new div() {{
                        className = "line";
                    }};

                    new textarea() {{
                        innerHTML = codeAndView.code.replaceAll("\n", "&#10").replaceAll(" ", "&nbsp");
                        className = "code";
                        style.height = height + "px";
                    }};
                }};
    }

    private Component1<div, CodeView.CodeAndView> rendererWithCountString(int count, int height) {
        return rendererWithHeight(count * height);
    }

    record Data(int x, String y) {
    }

    @Override
    public div mount() {
        return new div() {{
            new div() {{
                className = "header";


                new div() {{
                    className = "menu-element";
                    new img() {{
                        style.maxWidth = "140px";
                        src = "https://pngimg.com/uploads/letter_g/letter_g_PNG59.png";
                    }};
                }};

                new div() {{
                    className = "menu-element";
                    new h1("react") {{
                        className = "h1-main";
                    }};
                }};

                new div() {{
                    className = "desc-element";
                    style.textAlign = "right";
                    new h2("Компоненты") {{
                        className = "info-main";
                    }};

                    new h4("На этой странице предоставлена витрина основных UI элементов Greact-a") {{
                        className = "info-desc";
                    }};
                }};
            }};
            new div() {
                {
                    className = "main";

                    new div() {{
                        className = "menu";

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

                        new a("Div") {{
                            className = "menu-item";
                            href = "#div";
                        }};

                        new a("Tabs") {{
                            className = "menu-item";
                            href = "#tabs";
                        }};

                        new a("Grid") {{
                            className = "menu-item";
                            href = "#grid";
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

                                new CodeView(() -> new div() {{
                                    new h1("h1 text") {{
                                        style.color = "#703191";
                                    }};
                                }}, rendererWithCountString(3, 20));
                            }};

                            new div() {{
                                className = "vision-code";

                                new h3("ПРИМЕР") {{
                                    className = "ex-text";
                                }};

                                new CodeView(() -> new div() {{
                                    new h2("h2 text") {{
                                        style.color = "#293133";
                                    }};
                                }}, rendererWithCountString(3, 20));
                            }};

                            new div() {{
                                className = "vision-code";

                                new h3("ПРИМЕР") {{
                                    className = "ex-text";
                                }};

                                new CodeView(() -> new div() {{
                                    new h3("h3 text") {{
                                        style.color = "#44944A";
                                    }};
                                }}, rendererWithCountString(3, 20));
                            }};

                            new div() {{
                                className = "vision-code";

                                new h3("ПРИМЕР") {{
                                    className = "ex-text";
                                }};

                                new CodeView(() -> new div() {{
                                    new h4("h4 text") {{
                                        style.color = "#B03F35";
                                    }};
                                }}, rendererWithCountString(3, 20));
                            }};

                            new div() {{
                                className = "vision-code";

                                new h3("ПРИМЕР") {{
                                    className = "ex-text";
                                }};

                                new CodeView(() -> new div() {{
                                    new h5("h5 text") {{
                                        style.color = "#4285B4";
                                    }};
                                }}, rendererWithCountString(3, 20));
                            }};
                        }};

                        new div() {
                            {
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

                                    new CodeView(() -> new div() {{
                                        new img() {{
                                            src = "https://clck.ru/YFWWu";
                                        }};
                                    }}, rendererWithCountString(3, 20));
                                }};
                            }
                        };

                        new div() {
                            {
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

                                    new CodeView(() -> new div() {{
                                        new button("Click It!") {{
                                            style.color = "#a675b3";
                                            style.width = "150px";
                                            style.height = "30px";
                                            style.backgroundColor = "#3b2751";
                                        }};
                                    }}, rendererWithCountString(6, 20));
                                }};
                            }
                        };

                        new div() {
                            {
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

                                    new CodeView(() -> new div() {{
                                        new a("Link") {{
                                            href = "#blablabla";
                                            style.color = "#609123";
                                        }};
                                    }}, rendererWithCountString(4, 20));
                                }};
                            }
                        };

                        new div() {
                            {
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

                                    new CodeView(() -> new div() {{
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
                                        }};
                                    }}, rendererWithCountString(18, 20));
                                }};
                            }
                        };

                        new div() {
                            {
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

                                    new CodeView(() -> new div() {{
                                        new ul() {{
                                            style.color = "#6e2f8e";
                                            new li() {{
                                                innerText = "First";
                                            }};
                                            new li() {{
                                                innerText = "Second";
                                            }};
                                        }};
                                    }}, rendererWithCountString(9, 20));
                                }};
                            }
                        };

                        new div() {
                            {
                                id = "select";
                                className = "example";

                                new h2("Пример использования всплывающего списока") {{
                                    className = "heading";
                                }};

                                new div() {{
                                    className = "vision-code";

                                    new h3("ПРИМЕР") {{
                                        className = "ex-text";
                                    }};

                                    new CodeView(() -> new div() {{
                                        new select() {{
                                            new option("First") {{
                                                style.color = "#682887";
                                            }};
                                            new option("Second") {{
                                                style.color = "#682887";
                                            }};
                                        }};
                                    }}, rendererWithCountString(8, 20));
                                }};
                            }
                        };

                        new div() {
                            {
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

                                    new CodeView(() -> new div() {{
                                        new textarea() {{
                                            style.backgroundColor = "#fff";
                                            style.border = "3px solid #682887";
                                        }};
                                    }}, rendererWithCountString(4, 20));
                                }};
                            }
                        };

                        new div() {
                            {
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

                                    new CodeView(() -> new div() {{
                                        new input() {{
                                            style.backgroundColor = "#00CC00";
                                            style.border = "3px solid #1240AB";
                                        }};
                                    }}, rendererWithCountString(4, 20));
                                }};
                            }
                        };

                        new div() {
                            {
                                id = "div";
                                className = "example";

                                new h2("Пример использования div") {{
                                    className = "heading";
                                }};

                                new div() {{
                                    className = "vision-code";

                                    new h3("ПРИМЕР") {{
                                        className = "ex-text";
                                    }};

                                    new CodeView(() -> new div() {{
                                        new div() {{
                                            style.backgroundColor = "#682887";
                                            style.height = "200px";
                                            style.width = "200px";
                                        }};
                                    }}, rendererWithCountString(5, 20));
                                }};
                            }
                        };

                        new div() {
                            {
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

                                    new CodeView(() -> new div() {{
                                        new Tabs(new Tab("ex1", () -> new div() {{
                                            new h1("Hello");
                                        }}), new Tab("ex2", () -> new div() {{
                                            new h1("World");
                                        }}));
                                    }}, rendererWithCountString(5, 20));
                                }};
                            }
                        };

                        new div() {
                            {
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

                                    new CodeView(() -> new div() {{
                                        new div() {{
                                            var data = Array.of(
                                                new Data(1, "One"),
                                                new Data(2, "Two"),
                                                new Data(3, "Three")
                                            );

                                            new Grid<>(data) {{
                                                adjust(Data::x).name("the X");
                                            }};
                                        }};
                                    }}, rendererWithCountString(9, 20));
                                }};
                            }
                        };
                    }};
                }
            };

            new div() {{
                className = "footer";
                new h3("Создано на Greact") {{
                    className = "footer-text";
                    style.color = "#fff";
                    style.textAlign = "center";
                }};
            }};
        }};
    }
}
