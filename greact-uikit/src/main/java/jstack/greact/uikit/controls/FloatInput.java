package jstack.greact.uikit.controls;

import jstack.greact.html.input;
import jstack.jscripter.transpiler.model.JSExpression;

public class FloatInput extends Input<Float> {

    public FloatInput() {super("number");}

    @Override protected Float parseValueOpt(input src) {
        float newVal = JSExpression.of("parseFloat(src.value)");
        return JSExpression.<Boolean>of("isNaN(newVal)") ? null : newVal;
    }
}
