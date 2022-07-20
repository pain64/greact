package com.over64.greact.uikit.samples.js;

import com.greact.model.async;
import com.over64.TypesafeSql;
import com.over64.TypesafeSql.Table;
import com.over64.greact.dom.HTMLNativeElements.Component0;
import com.over64.greact.dom.HTMLNativeElements.div;
import com.over64.greact.uikit.Grid;

import static com.over64.greact.uikit.samples.Main.Server.server;

public class MainPage implements Component0<div> {
    @Table("teachers") public record StudyForm(@TypesafeSql.Id long school_id, String name, String email, int age) {}

    @Override @async public div mount() {
        var studyForms = server(db -> db.select(StudyForm.class));
        server(db -> db.deleteSelf(new StudyForm(2, "a", "sds", 20)));
        server(db -> db.updateSelf(new StudyForm(1, "a", "sds", 20)));
        return new div() {{
            new Grid<>(studyForms) {{
                adjust(StudyForm::school_id).noedit();
            }};
        }};
    }
}