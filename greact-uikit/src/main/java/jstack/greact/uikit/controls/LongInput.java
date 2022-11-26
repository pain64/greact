package jstack.greact.uikit.controls;

import jstack.jscripter.transpiler.model.JSExpression;
import jstack.greact.dom.HTMLNativeElements;

public class LongInput extends Input<Long> {
    public LongInput() {super("number");}

    @Override protected Long parseValueOpt(HTMLNativeElements.input src) {
        long newVal = JSExpression.of("parseInt(src.value)");
        return JSExpression.<Boolean>of("isNaN(newVal)") ? null : newVal;
    }
}