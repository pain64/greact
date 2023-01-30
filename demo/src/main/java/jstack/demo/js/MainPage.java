package jstack.demo.js;

import jstack.greact.dom.HTMLNativeElements.*;
import jstack.greact.uikit.Grid;
import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.Require;
import jstack.jscripter.transpiler.model.async;
import jstack.ssql.schema.Id;
import jstack.ssql.schema.Sequence;
import jstack.ssql.schema.Table;

import static jstack.demo.Main.Server.server;

@Require.CSS("main_page.css")
public class MainPage implements Component0<div> {
    @Table("teachers") public record Teacher(
        @Id @Sequence("teacher_id_seq") long id, String name, String email, int age
    ) { }

    @Override @async public div mount() {
        server(db -> {
            db.exec("update teachers set age = 18 where age = :1", 14);
            db.query(Teacher.class, "select * from teachers");
            db.query(Integer.class, "select min(age) from teachers where age > :1", 42);
            var t = db.insertSelf(new Teacher(1L, "John", "some", 40));
            db.updateSelf(t);
            db.deleteSelf(t);
            return null;
        });

        return new div() {{
            new h2("Grid usage example");

            new div() {{
                var data = server(db -> db.select(Teacher.class));
                new Grid<>(data) {{
                    onRowChange = row -> JSExpression.of("console.log(row)");
                }};
            }};
        }};
    }
}
