package com.over64.greact.uikit.controls;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.input;

public class CheckBox extends Input<Boolean> {

    public CheckBox() { super("checkbox"); }

    public CheckBox label(String lbl) {
        this._label = lbl;
        return this;
    }

    @Override protected Boolean parseValueOpt(input src) {
        return JSExpression.of("src.checked");
    }

    @Override protected String valueToHtmlValue() {
        return value ? "on" : "off";
    }
}
