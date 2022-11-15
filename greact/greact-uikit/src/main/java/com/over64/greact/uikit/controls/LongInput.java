package com.over64.greact.uikit.controls;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements;

public class LongInput extends Input<Long> {
    public LongInput() {super("number");}

    @Override protected Long parseValueOpt(HTMLNativeElements.input src) {
        long newVal = JSExpression.of("parseInt(src.value)");
        return JSExpression.<Boolean>of("isNaN(newVal)") ? null : newVal;
    }
}