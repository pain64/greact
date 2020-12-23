package greact.sample.plainjs.demo;

import com.greact.model.JSExpression;
import com.greact.model.async;
import com.over64.greact.dom.HTMLNativeElements.*;

public class UsersPage implements Component0<body> {

    @async <T> T get(String url) {
        return JSExpression.of("""
            (await (await fetch(url)).json())""");
    }

    String userName = "";

    @Override public body mount() {
        get("/users?nameLike=" + userName);
        get("/userInfo?id=1");

        return new body() {{
           new h1("hello, world!");
        }};
    }
}
