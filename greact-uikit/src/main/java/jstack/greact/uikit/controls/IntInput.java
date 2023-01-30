package jstack.greact.uikit.controls;

import jstack.greact.dom.HTMLNativeElements.input;
import jstack.jscripter.transpiler.model.JSExpression;

public class IntInput extends Input<Integer> {
    public IntInput() {super("number");}

    @Override protected Integer parseValueOpt(input src) {
        int newVal = JSExpression.of("parseInt(src.value)");
        return JSExpression.<Boolean>of("isNaN(newVal)") ? null : newVal;
    }
}
