package com.over64.greact.uikit.samples.js;

import com.over64.TypesafeSql.Id;
import com.over64.TypesafeSql.Sequence;
import com.over64.TypesafeSql.Table;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.uikit.Grid;

import static com.over64.greact.uikit.samples.Main.Server.server;

public class MainPage implements Component0<div> {
    @Table("и_формы_обучения")
    record StudyForm(
            @Id @Sequence("и_фо_ид_посл") long ид, String наименование) {
    }

    @Override
    public div mount() {
        server(db -> db.select(StudyForm.class, "order by ид desc"));
        return new div() {{
            new h1("1yy23");
        }};
    }
}