package greact.sample.plainjs.demo;

import com.over64.greact.dom.HTMLNativeElements.*;
import greact.sample.plainjs.demo.searchbox.*;
import greact.sample.plainjs.demo.searchbox._01impl.Cascade;
import greact.sample.plainjs.demo.searchbox._01impl.CheckBox;
import greact.sample.plainjs.demo.searchbox._01impl.Select;
import greact.sample.plainjs.demo.searchbox._01impl.StrInput;

import static greact.sample.SuperDemo.Server.server;

public class UsersPage implements Component0<body> {
    record User(long id, String name, int age, String sex) {}

    record UserInfo(String faculty, String address, String phone) {}

    /**
     * @param <T> dangling, but will be checked by JScripter
     * @param <V> type of field value
     */
    interface MemberRef<T, V> {
        default String memberName() {return "must be autogenerated";}

        V value();
    }

    @Override
    public body mount() {
        return new body() {{
            new Grid<User>() {{
                data = new SearchBox<>(
                    new StrInput()
                        .label("Имя студента"),
                    new CheckBox()
                        .label("Включить"),
                    new Cascade<>(
                        new Select<>(new String[]{"М", "Ж"})
                            .label("Пол"),
                        sex ->
                            new Select<>(new String[]{"Омск", "Москва"})
                                .label("Адрес")),
                    (name, isEnabled, address) -> server(db -> db.array(
                        "SELECT * FROM users WHERE name like :1",
                        User.class, "%" + name + "%")));
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
                        if (info != null) new span("Адрес: " + info.address);
                        else new span("Нет данных о студенте");
                    }};
                };
            }};
        }};
    }
}