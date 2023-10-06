package jstack.greact.uikit;

import jstack.greact.html.*;
import jstack.jscripter.transpiler.model.Require;

@Require.CSS("table.css")
public class Table implements Component0<div> {
    public Component0<table> content = new table();
    public String title = null;

    @Override public Component0<div> render() {
        var self = this;
        return new div("uk-table") {{
            if (self.title != null) new h4(self.title);
            new slot<>(content);
        }};
    }
}
