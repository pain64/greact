package com.over64.greact.uikit.controls;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.input;

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
