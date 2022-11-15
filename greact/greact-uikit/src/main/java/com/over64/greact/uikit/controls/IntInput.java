package com.over64.greact.uikit.controls;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.input;

public class IntInput extends Input<Integer> {
    public IntInput() {super("number");}

    @Override protected Integer parseValueOpt(input src) {
        int newVal = JSExpression.of("parseInt(src.value)");
        return JSExpression.<Boolean>of("isNaN(newVal)") ? null : newVal;
    }
}
