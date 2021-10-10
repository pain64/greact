package com.over64.greact.uikit.controls;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.input;

public class FloatInput extends Input<Float> {

    public FloatInput() {super("number");}

    @Override protected Float parseValueOpt(input src) {
        float newVal = JSExpression.of("parseFloat(src.value)");
        return JSExpression.<Boolean>of("isNaN(newVal)") ? null : newVal;
    }
}
