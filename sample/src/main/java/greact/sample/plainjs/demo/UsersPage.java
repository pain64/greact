package greact.sample.plainjs.demo;

import com.over64.greact.dom.HTMLNativeElements.*;

import static greact.sample.SuperDemo.Server.server;

public class UsersPage implements Component0<body> {
    String nameLike = "";
    User[] users = new User[]{};

    @Override public body mount() {
        return new body() {{
            new input() {{
                style.maxWidth = "300px";
                placeholder = "Имя студента...";
                onchange = ev -> nameLike = ev.target.value;
            }};
            new button("искать") {{
                onclick = ev -> effect(users = server(db -> db.array(
                    "SELECT id, name, age, sex FROM users WHERE name like :1",
                    User.class, "%" + nameLike + "%")));
            }};
            new Grid<>(users) {{
                columns = new Column[]{
                    new Column<User>("Id", c -> c.id),
                    new Column<User>("Имя", c -> c.name),
                    new Column<User>("Возраст", c -> c.age),
                    new Column<User>("Пол", c -> c.sex)
                };
                selectedRow = user -> {
                    var info = server(db -> db.uniqueOrNull(
                        "SELECT faculty, address, phone FROM user_info WHERE user_id = :1",
                        UserInfo.class, user.id));
                    return new div() {{
                        if (info != null) new h5("Адрес: " + info.address);
                        else new h5("Нет данных о студенте");
                    }};
                };
            }};
        }};
    }
}
