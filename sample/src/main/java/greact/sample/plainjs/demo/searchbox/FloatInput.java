package greact.sample.plainjs.demo.searchbox;

import com.greact.model.JSExpression;

public class FloatInput extends Input<Float> {

    @Override Float parseValueOpt(String src) {
        float newVal = JSExpression.of("parseFloat(src)");
        return JSExpression.<Boolean>of("newVal !== NaN") ? newVal : null;
    }
}
