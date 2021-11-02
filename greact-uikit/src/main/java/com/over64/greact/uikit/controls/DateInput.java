package com.over64.greact.uikit.controls;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.input;
import java.util.Date;

public class DateInput extends Input<Date> {
    public DateInput() {super("date");}

    @Override protected Date parseValueOpt(input src) {
        JSExpression.of("console.log(src.value)"); // test
        return JSExpression.of("new Date(src.value).toLocaleDateString()");
    }
}
