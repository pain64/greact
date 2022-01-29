package com.over64.greact.uikit.controls;

import com.greact.model.Require;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.uikit.Dates;

import java.util.Date;

@Require.CSS("date_input.css")
public class DateInput extends Control<Date> {
    @Override public Control child() {return null;}

    @Override public div mount() {
        var self = this;

        return new div() {{
            new label() {{
                className = "date-input";

                new span(label) {{
                    className = "date-input-span";
                }};
                new input() {{
                    className = "date-input-body";
                    type = "date";
                    valueAsNumber = Dates.getTime(self.value);


                    onchange = ev -> {
                        value = ev.target.value;
                        ready = optional || value != null;
                        onReadyChanged.run();

                        self.value = Dates.fromUnixTime(ev.target.valueAsNumber);
                    };
                }};
            }};
        }};
    }
}
