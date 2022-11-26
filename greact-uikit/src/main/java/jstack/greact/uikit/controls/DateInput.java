package jstack.greact.uikit.controls;

import jstack.jscripter.transpiler.model.Require;
import jstack.greact.uikit.Dates;
import jstack.greact.dom.HTMLNativeElements;

import java.util.Date;

@Require.CSS("date_input.css")
public class DateInput extends Control<Date> {
    @Override public Control child() {return null;}

    @Override public HTMLNativeElements.div mount() {
        var self = this;

        return new HTMLNativeElements.div() {{
            new HTMLNativeElements.label() {{
                className = "date-input";

                new HTMLNativeElements.span(label) {{
                    className = "date-input-span";
                }};
                new HTMLNativeElements.input() {{
                    className = "date-input-body";
                    type = "date";
                    valueAsNumber = Dates.getTime(self.value);


                    onchange = ev -> {
                        value = ((HTMLNativeElements.input) ev.target).value;
                        ready = optional || value != null;
                        onReadyChanged.run();

                        self.value = Dates.fromUnixTime(((HTMLNativeElements.input) ev.target).valueAsNumber);
                    };
                }};
            }};
        }};
    }
}
