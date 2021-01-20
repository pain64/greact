package greact.sample.plainjs.demo.searchbox._01impl;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.input;
import greact.sample.plainjs.demo.searchbox._00base._01Input;

public class FloatInput extends _01Input<Float> {

    public FloatInput() {super("number");}

    @Override protected Float parseValueOpt(input src) {
        float newVal = JSExpression.of("parseFloat(src.value)");
        return JSExpression.<Boolean>of("isNaN(newVal)") ? null : newVal;
    }
}
