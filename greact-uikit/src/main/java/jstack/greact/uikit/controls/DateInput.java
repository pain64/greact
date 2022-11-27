package jstack.greact.uikit.controls;

import jstack.greact.dom.HTMLNativeElements.div;
import jstack.greact.dom.HTMLNativeElements.input;
import jstack.greact.dom.HTMLNativeElements.label;
import jstack.greact.dom.HTMLNativeElements.span;
import jstack.jscripter.transpiler.model.Require;
import jstack.greact.uikit.Dates;

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
                        value = ((input) ev.target).value;
                        ready = optional || value != null;
                        onReadyChanged.run();

                        self.value = Dates.fromUnixTime(((input) ev.target).valueAsNumber);
                    };
                }};
            }};
        }};
    }
}
