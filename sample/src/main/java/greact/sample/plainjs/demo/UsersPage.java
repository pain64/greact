package greact.sample.plainjs.demo;

import com.over64.greact.dom.HTMLNativeElements.*;
import greact.sample.plainjs.demo.searchbox.*;
import greact.sample.plainjs.demo.searchbox._01impl.StrInput;
import greact.sample.server.DbUtil;

import static greact.sample.SuperDemo.Server.server;

public class UsersPage implements Component0<body> {
    record User(long id, String name, int age, String sex) {}
    record UserInfo(String faculty, String address, String phone) {}

    @Override public body mount() {
        return new body() {{
            new SearchBox<>(
                new StrInput().label("Имя студентаs").optional(),
                name -> server(db -> db.array(
                    "SELECT * FROM users WHERE name like :1", User.class, DbUtil.like(name)))) {{
                view = new GridSlot<>() {{
                    columns = new Column[]{
                        new Column<>("Id", User::id),
                        new Column<>("Имя", User::name),
                        new Column<>("Возраст", User::age),
                        new Column<>("Пол", User::sex)
                    };
                    onRowDelete = user -> server(db -> db.exec("DELETE FROM users WHERE id = :1", user.id));
                    expandedRow = user -> {
                        var info = server(db -> db.uniqueOrNull(
                            "SELECT faculty, address, phone FROM user_info WHERE user_id = :1",
                            UserInfo.class, user.id));
                        return new div() {{
                            if (info != null) new span("Адрес: " + info.address);
                            else new span("Нет данных о студенте");
                        }};
                    };
                }};
            }};
        }};
    }
}
