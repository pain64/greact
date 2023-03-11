package jstack.demo.js;

import jstack.greact.dom.HTMLNativeElements.Component0;
import jstack.greact.dom.HTMLNativeElements.div;
import jstack.greact.dom.HTMLNativeElements.h2;
import jstack.greact.uikit.Grid;
import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.Require;
import jstack.jscripter.transpiler.model.async;

import static jstack.demo.Dialect.*;
import static jstack.demo.Main.Server.server;

@Require.CSS("main_page.css")
public class MainPage implements Component0<div> {
    record Teacher(
        long id, String name, String email, int age, Gender gender, Term term
    ) { }

    @Override @async public div mount() {
        return new div() {{
            new h2("Grid usage example");

            new div() {{
                var data = server(db -> db.query("select * from teachers", Teacher.class));
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
