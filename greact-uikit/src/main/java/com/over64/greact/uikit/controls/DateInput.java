package com.over64.greact.uikit.controls;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.input;
import com.over64.greact.uikit.Dates;

public class DateInput extends Input<Dates> {
    public DateInput() {super("date");}

    @Override protected Dates parseValueOpt(input src) {
        JSExpression.of("console.log(src.value)"); // test
        return JSExpression.of("new Date(src.value).toLocaleDateString()");
    }
}
