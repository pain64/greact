package com.over64.greact.uikit.samples.js;

import com.greact.model.async;
import com.over64.TypesafeSql.Table;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.uikit.Grid;

import java.util.Date;

import static com.over64.greact.uikit.samples.Main.Server.server;

public class MainPage implements Component0<div> {
    @Table("teachers") record StudyForm(long school_id, String name, String email, int age) {}

    @Override @async public div mount() {
        var studyForms = server(db -> db.select(StudyForm.class));
        return new div() {{
            new Grid<>(studyForms) {{
                adjust(StudyForm::school_id).noedit();
                onRowAdd = sf -> server(db -> db.insertSelf(sf));
                onRowChange = sf -> server(db -> db.updateSelf(sf));
                onRowDelete = sf -> server(db -> db.deleteSelf(sf));
            }};
        }};
    }
}