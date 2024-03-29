package jstack.greact.uikit.controls;

import jstack.greact.html.div;
import jstack.greact.html.input;
import jstack.greact.html.label;
import jstack.greact.html.span;
import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.Require;

@Require.CSS("input.css")
public abstract class Input<T> extends Control<T> {
    int maxWidth = 0;
    int maxLength = 0;
    final String type;

    protected Input(String type) {
        this.type = type;
        if(optional || type.equals("checkbox")) {
            this.ready = true;
            this.onReadyChanged.run();
        }
    }
    protected abstract T parseValueOpt(input src);
    protected String valueToHtmlValue() {
        return  value.toString();
    }

    static <T> boolean isNullOrUndefined(T value) {
        return JSExpression.of("typeof :1 === 'undefined' || :1 === null", value);
    }

    @Override public Control child() { return null; }

    @Override public div render() {
        var self = this;
        return new div() {{
//            style.alignItems = "center";
            //style.padding = "0px 2px";
         //   style.margin = "0px 10px 0px 0px";
            new label() {{
                className = "input-body";
//                if(self instanceof CheckBox)
//                    style.margin = "3px 0px 0px 0px";
//                if(self instanceof CheckBox)
//                    style.display = "inline-block";

//                style.height = "100%";

                style.display = "flex";
                style.alignItems = "center";

                new span(label) {{
                    style.whiteSpace = "nowrap";
                    style.margin = "0px 5px 0px 0px";
                }};
                new input() {{
                    //className = "form-check-input";
                    // FIXME: вот это вот - костыль для CheckBox
                    if(self.type != "checkbox") className = "input-body-checkbox";
                    else className = "form-check-input";
                    type = self.type;
                    value = isNullOrUndefined(self.value) ? null : valueToHtmlValue();
                    onclick = ev -> ev.stopPropagation();
                    // FIXME: вот это вот - костыль для CheckBox
                    checked = (Boolean) self.value;
                    onchange = ev -> {
                        self.value = parseValueOpt((input) ev.target);
                        self.ready = self.optional || self.value != null;
                        self.onReadyChanged.run();
                    };
                }};
            }};
        }};
    }
}
