package com.over64.greact.uikit.controls;

import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.uikit.Dates;

import java.util.Date;

public class DateInput extends Control<Date> {
    @Override public Control child() {return null;}

    @Override public div mount() {
        var self = this;

        return new div() {{
            new label() {{
                style.display = "flex";
                style.alignItems = "center";
                style.whiteSpace = "nowrap";

                new span(_label) {{
                    style.margin = "0px 5px 0px 0px";
                }};
                new input() {{
                    style.width = "100%";
                    type = "date";
                    valueAsNumber = Dates.getTime(self.value);


                    onchange = ev -> {
                        value = ev.target.value;
                        ready = _optional || value != null;
                        onReadyChanged.run();

                        self.value = Dates.fromUnixTime(ev.target.valueAsNumber);
                    };
                }};
            }};
        }};
    }
}
