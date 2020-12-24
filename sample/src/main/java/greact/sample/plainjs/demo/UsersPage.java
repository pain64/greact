package greact.sample.plainjs.demo;

import com.greact.model.JSExpression;
import com.greact.model.async;
import com.over64.greact.dom.HTMLNativeElements.*;

public class UsersPage implements Component0<body> {

    @async <T> T get(String url) {
        return JSExpression.of("""
            (await (await fetch(url)).json())""");
    }

    void log(Object obj) {
        JSExpression.of("console.log(obj)");
    }

    String nameLike = "";
    User[] users = new User[]{};

    @Override public body mount() {
        return new body() {{
            new input() {{
                className = "form-control";
                style.maxWidth = "300px";
                style.display = "inline";
                style.margin = "5px";
                placeholder = "Имя студента...";
                onchange = ev -> nameLike = ev.target.value;
            }};
            new button("искать") {{
                className = "btn";
                style.margin = "5px";
                onclick = ev -> effect(users = get("/users?nameLike=" + nameLike));
            }};
            new Grid<>(users) {{
                columns = new Column[]{
                    new Column<User>("Id", c -> c.id),
                    new Column<User>("Имя", c -> c.name),
                    new Column<User>("Возраст", c -> c.age),
                    new Column<User>("Пол", c -> c.sex)
                };
                selectedRow = user -> {
                    UserInfo info = get("/userInfo?id=" + user.id);
                    return new div() {{
                        if (info != null) new h2("Адрес: " + info.address);
                        else new h2("Нет данных о студенте");
                    }};
                };
            }};
        }};
    }
}
