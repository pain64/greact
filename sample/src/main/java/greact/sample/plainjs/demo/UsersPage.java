package greact.sample.plainjs.demo;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.over64.greact.dom.Globals;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.rpc.RPC;
import greact.sample.server.TypesafeSql;

import java.util.List;

public class UsersPage implements Component0<body> {
    public static class Endpoint0 implements RPC.Endpoint<TypesafeSql> {

        @Override
        public Object handle(TypesafeSql db, Gson gson, List<JsonElement> args) {
            var nameLike = args.get(0).getAsString();
            return db.array(
                "SELECT id, name, age, sex FROM users WHERE name like :1",
                User.class, nameLike);
        }
    }

    public static class Endpoint1 implements RPC.Endpoint<TypesafeSql> {

        @Override
        public Object handle(TypesafeSql db, Gson gson, List<JsonElement> args) {
            var id = args.get(0).getAsInt();
            return db.uniqueOrNull(
                "SELECT faculty, address, phone FROM user_info WHERE user_id = :1",
                UserInfo.class, id);
        }
    }

    String nameLike = "";
    User[] users = new User[]{};

    @Override
    public body mount() {
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
//                onclick = ev -> effect(users = server(db -> db.array(
//                    "SELECT id, name, age, sex FROM users WHERE name like :1",
//                    User.class, "%" + nameLike + "%")));
                onclick = ev -> effect(users = Globals.doRemoteCall("/rpc",
                    "greact.sample.plainjs.demo.UsersPage$Endpoint0",
                    "%" + nameLike + "%"));
            }};
            new Grid<>(users) {{
                columns = new Column[]{
                    new Column<User>("Id", c -> c.id),
                    new Column<User>("Имя", c -> c.name),
                    new Column<User>("Возраст", c -> c.age),
                    new Column<User>("Пол", c -> c.sex)
                };
                selectedRow = user -> {
//                    var info = server(db -> db.uniqueOrNull(
//                        "SELECT faculty, address, phone FROM user_info WHERE user_id = :1",
//                        UserInfo.class, user.id));
                    UserInfo info =  Globals.doRemoteCall("/rpc",
                        "greact.sample.plainjs.demo.UsersPage$Endpoint1", user.id);

                    return new div() {{
                        if (info != null) new h2("Адрес: " + info.address);
                        else new h2("Нет данных о студенте");
                    }};
                };
            }};
        }};
    }
}
