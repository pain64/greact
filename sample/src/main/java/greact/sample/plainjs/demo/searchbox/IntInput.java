package greact.sample.plainjs.demo.searchbox;

import com.greact.model.JSExpression;

public class IntInput extends Input<Integer> {
    @Override Integer parseValueOpt(String src) {
        int newVal = JSExpression.of("parseInt(src)");
        return JSExpression.<Boolean>of("newVal !== NaN") ? newVal : null;
    }
}
