package jstack.demo.js;

import jstack.greact.dom.HTMLNativeElements.*;
import jstack.greact.uikit.Grid;
import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.Require;
import jstack.jscripter.transpiler.model.async;
import jstack.ssql.schema.Id;
import jstack.ssql.schema.Ordinal;
import jstack.ssql.schema.Sequence;
import jstack.ssql.schema.Table;

import java.math.BigDecimal;

import static jstack.demo.Main.Server.server;

@Require.CSS("main_page.css")
public class MainPage implements Component0<div> {
    enum Gender {MALE, FEMALE}
    @Ordinal enum Term {SPRING, AUTUMN}

    @Table("teachers") record Teacher(
        @Id @Sequence("teacher_id_seq") long id, String name, String email,
        int age, Gender gender, Term term
    ) { }


    @Override @async public div mount() {
        server(db -> {
            //db.exec("update teachers set age = 18 where age = :1", 14);
            db.query(Teacher.class, "select * from teachers");
           // db.query(Integer.class, "select min(age) from teachers where age > :1", 42);
           // var t = db.insertSelf(new Teacher(1L, "John", "some", 40, Gender.MALE, Term.SPRING));
           // db.upsert(t);
           // db.updateSelf(t);
           // db.deleteSelf(t);
            return null;
        });

        return new div() {{
            new h2("Grid usage example");

            new div() {{
                var data = server(db -> db.select(Teacher.class));
                new Grid<>(data) {{
                    adjust(Teacher::term).view(t -> switch (t) {
                        case SPRING -> "Весна";
                        case AUTUMN -> "Осень";
                    });
                    onRowChange = row -> JSExpression.of("console.log(row)");
                }};
            }};
        }};
    }
}
