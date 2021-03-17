package greact.sample.plainjs.demo;

import com.over64.greact.dom.HTMLNativeElements.*;
import greact.sample.plainjs.demo.searchbox.*;
import greact.sample.plainjs.demo.searchbox._01impl.IntInput;
import greact.sample.plainjs.demo.searchbox._01impl.Select;
import greact.sample.plainjs.demo.searchbox._01impl.StrInput;
import greact.sample.server.DbUtil;

import static greact.sample.SuperDemo.Server.server;

public class UsersPage implements Component0<body> {
    record User(long id, String name, int age, String sex) {}
    record UserInfo(String faculty, String address, String phone) {}

    @Override public body mount() {
        return new body() {{
            new SearchBox<>(
                new StrInput().label("Имя студента").optional(),
                name -> server(db -> db.array(
                    "SELECT * FROM users WHERE name like :1 order by id desc", User.class, DbUtil.like(name)))) {{
                view = new GridSlot<>() {{
                    columns = new Column[]{
                        new Column<>("Id", User::id),
                        new Column<>("Имя", User::name)
                            .editable(new StrInput()),
                        new Column<>("Возраст", User::age)
                            .editable(new IntInput()),
                        new Column<>("Пол", User::sex)
                            .editable(new Select<>(new String[]{"М", "Ж"}))
                    };
                    onRowAdd = user -> server(db ->
                        db.exec("insert into users(id, name, age, sex) values(nextval('users_id_seq'), :1, :2, :3)",
                            user.name, user.age, user.sex));
                    onRowChange = user -> server(db ->
                        db.exec("UPDATE users set name = :1, age = :2, sex = :3 WHERE id = :4",
                            user.name, user.age, user.sex, user.id));
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
