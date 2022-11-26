package jstack.greact.uikit.controls;

import jstack.jscripter.transpiler.model.JSExpression;
import jstack.greact.dom.HTMLNativeElements;

public class IntInput extends Input<Integer> {
    public IntInput() {super("number");}

    @Override protected Integer parseValueOpt(HTMLNativeElements.input src) {
        int newVal = JSExpression.of("parseInt(src.value)");
        return JSExpression.<Boolean>of("isNaN(newVal)") ? null : newVal;
    }
}
