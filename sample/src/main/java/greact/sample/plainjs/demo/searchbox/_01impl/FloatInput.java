package greact.sample.plainjs.demo.searchbox._01impl;

import com.greact.model.JSExpression;
import greact.sample.plainjs.demo.searchbox._00base._01Input;

public class FloatInput extends _01Input<Float> {

    public FloatInput() {super("number");}

    @Override protected Float parseValueOpt(String src) {
        float newVal = JSExpression.of("parseFloat(src)");
        return JSExpression.<Boolean>of("isNaN(newVal)") ? null : newVal;
    }
}
