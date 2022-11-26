package jstack.greact.uikit.controls;

import jstack.jscripter.transpiler.model.JSExpression;
import jstack.greact.dom.HTMLNativeElements;

public class FloatInput extends Input<Float> {

    public FloatInput() {super("number");}

    @Override protected Float parseValueOpt(HTMLNativeElements.input src) {
        float newVal = JSExpression.of("parseFloat(src.value)");
        return JSExpression.<Boolean>of("isNaN(newVal)") ? null : newVal;
    }
}
