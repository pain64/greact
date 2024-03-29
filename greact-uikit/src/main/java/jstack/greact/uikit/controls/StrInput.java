package jstack.greact.uikit.controls;

import jstack.greact.html.input;
import jstack.jscripter.transpiler.model.JSExpression;

public class StrInput extends Input<String> {
    public StrInput() {super("text");}

    public StrInput label(String lbl) {
        this.label = lbl;
        return this;
    }

    public StrInput optional() {
        this.optional = true;
        this.ready = true;
        return this;
    }

    @Override protected String parseValueOpt(input src) {
        return JSExpression.of("src.value === ''") ? null : src.value;
    }
}
