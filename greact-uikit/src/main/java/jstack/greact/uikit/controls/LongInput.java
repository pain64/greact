package jstack.greact.uikit.controls;

import jstack.greact.dom.HTMLNativeElements.input;
import jstack.jscripter.transpiler.model.JSExpression;

public class LongInput extends Input<Long> {
    public LongInput() {super("number");}

    @Override protected Long parseValueOpt(input src) {
        long newVal = JSExpression.of("parseInt(src.value)");
        return JSExpression.<Boolean>of("isNaN(newVal)") ? null : newVal;
    }
}