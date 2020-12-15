package com.over64.greact.alternative;

import com.over64.greact.dom.HTMLNativeElements.*;

class DemoGrid implements Component0<div> {
    static record UserInfo(String name, int age) {
    }

    @async public div mount() {
        UserInfo[] users = server(di ->
            di.commonDb.list("select name, age from users"));

        return new div() {{
            new Grid<>(users) {{
                row = user -> new many<>(
                    new td(user.name),
                    new td("" + user.age));
                selected = user -> new div(
                    new h1("selected user with name: " + user.name));
            }};
        }};
    }
}
