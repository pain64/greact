package com.over64.greact.alternative;

import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.dom.HtmlElement;

import java.util.Optional;

public class HW implements Component0<div> {
    int nUsers = 1;
    div d = new div(); // compilation error

    static class Decorated<T extends HtmlElement> implements Component0<div> {
        final Component0<T> forDecorate;

        Decorated(Component0<T> forDecorate) {
            this.forDecorate = forDecorate;
        }

        @Override
        public div mount() {
            return new div() {{
                style.border = "1px red solid";
                new slot<>(forDecorate);
            }};
        }
    }

    static class UltraFake implements Component1<a, String> {
        @Override
        public a mount(String text) {
            return new a("Fake greet you!, " + text);
        }
    }

    static String localId(String prefix) {
        return prefix + "bla bla bla";
    }

    @async public div mount() {
        // var $root = Globals.gReactElement;
        var nameOpt = server(di -> Optional.of("Ivan"));

        /*
           return Globals.gReactMount(() => {
                $root.innerText = "hello, Kitty";
           });
         */

        var linkClassName = localId("xxx");

        return new div() {{
            new style(
                "@media (min-height: 680px) {",
                "  .", linkClassName, "{",
                "    color: red;",
                "    background-color: black;",
                "  }",
                "}");

            new optional<>(nameOpt, name -> new a("Greet you, " + name));
            new optional<>(nameOpt, new Component1<>() {
                String name = "Sid Vicious";
                String age = "21";
                boolean dead = true;

                @Override
                public a mount(String s) {
                    new div() {{
                        new button("make alive") {{
                            onclick = () -> effect(dead = false);
                        }};
                    }};

                    long unixTime = server(di -> System.currentTimeMillis());
                    return new a("Greet you, " + name + "at " + unixTime) {{
                        className = linkClassName;
                    }};
                }
            });
            new optional<>(nameOpt, name -> {
                var state = new Object() {
                    String name = "Sid Vicious";
                    String age = "21";
                    boolean dead = true;
                };

                new div() {{
                    new button("make alive") {{
                        onclick = () -> effect(state.dead = false);
                    }};
                }};

                long unixTime = server(di -> System.currentTimeMillis());
                effect(unixTime);
                return new a("Greet you, " + name + "at " + unixTime);
            });
            new optional<>(nameOpt, new UltraFake());
            new h1("number of GReact users: " + nUsers);
            new button("increment") {{
                onclick = () -> effect(nUsers + 1);
            }};
        }};
    }
}
