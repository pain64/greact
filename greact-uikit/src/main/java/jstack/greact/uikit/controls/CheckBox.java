package jstack.greact.uikit.controls;

import jstack.jscripter.transpiler.model.JSExpression;
import jstack.greact.dom.HTMLNativeElements;

public class CheckBox extends Input<Boolean> {

    public CheckBox() { super("checkbox"); }

    public CheckBox label(String lbl) {
        this.label = lbl;
        return this;
    }

    @Override protected Boolean parseValueOpt(HTMLNativeElements.input src) {
        return JSExpression.of("src.checked");
    }

    @Override protected String valueToHtmlValue() {
        return value ? "on" : "off";
    }
}
