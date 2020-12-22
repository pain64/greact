package com.over64.greact.alternative;

import com.over64.greact.dom.HTMLNativeElements.*;

class DemoGrid implements Component0<div> {
    static record UserInfo(String name, int age) {
    }

    @async public div mount() {
        var users = server(di ->
            di.commonDb.<String, Integer>list2("select name, age from users"));

        return new div() {{
            new Grid<>(users) {{
                row = user -> new many<>(
                    new td(user.f1),
                    new td("" + user.f2));
                selected = user -> new div(
                    new h1("selected user with name: " + user.f1));
            }};
        }};
    }
}
