package greact.sample.plainjs.demo.searchbox._01impl;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.input;
import greact.sample.plainjs.demo.searchbox._00base._01Input;

public class IntInput extends _01Input<Integer> {
    public IntInput() {super("number");}

    @Override protected Integer parseValueOpt(input src) {
        int newVal = JSExpression.of("parseInt(src.value)");
        return JSExpression.<Boolean>of("isNaN(newVal)") ? null : newVal;
    }
}
