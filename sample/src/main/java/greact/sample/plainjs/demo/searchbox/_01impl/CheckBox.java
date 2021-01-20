package greact.sample.plainjs.demo.searchbox._01impl;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.input;
import greact.sample.plainjs.demo.searchbox._00base._01Input;

public class CheckBox extends _01Input<Boolean> {

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
