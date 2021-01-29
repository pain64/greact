package greact.sample.plainjs.demo.searchbox._01impl;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.input;
import greact.sample.plainjs.demo.searchbox._00base._01Input;

public class StrInput extends _01Input<String> {
    public StrInput() {super("text");}

    public StrInput label(String lbl) {
        this._label = lbl;
        return this;
    }

    public StrInput optional() {
        this._optional = true;
        this.ready = true;
        return this;
    }

    @Override
    protected String parseValueOpt(input src) {
        return JSExpression.of("src.value === ''") ? null : src.value;
    }
}
