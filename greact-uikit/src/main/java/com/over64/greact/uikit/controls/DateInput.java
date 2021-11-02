package com.over64.greact.uikit.controls;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.input;
import java.util.Date;

public class DateInput extends Input<Date> {
    public DateInput() {super("date");}

    @Override protected Date parseValueOpt(input src) {
        return JSExpression.of("new Date(Date.parse(src.value))");
    }
}
