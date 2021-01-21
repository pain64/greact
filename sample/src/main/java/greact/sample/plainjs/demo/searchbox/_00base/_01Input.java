package greact.sample.plainjs.demo.searchbox._00base;

import com.over64.greact.dom.HTMLNativeElements.*;

public abstract class _01Input<T> extends _00Control<T> {
    boolean required = true;
    int maxWidth = 0;
    int maxLength = 0;
    final String type;

    protected _01Input(String type) {this.type = type;}
    protected abstract T parseValueOpt(input src);
    protected String valueToHtmlValue() {
        return  value.toString();
    }

    @Override public _00Control child() { return null; }

    @Override public div mount() {
        var self = this;
        return new div() {{
            style.padding = "2px";
            new label() {{
                style.margin = "0";
                style.display = "flex";
                style.height = "100%";

                new span(_label) {{
                    style.display = "inline-flex";
                    style.alignItems = "center";
                    style.whiteSpace = "nowrap";
                    style.margin = "0px 5px 0px 0px";
                }};
                new input() {{
                    //className = "form-check-input";
                    type = self.type;
                    value = self.value == null ? null : valueToHtmlValue();
                    // FIXME: вот это вот - костыль для CheckBox
                    checked = (Boolean) self.value;
                    onchange = ev -> {
                        self.value = parseValueOpt(ev.target);
                        self.ready = self.value != null;
                        self.onReadyChanged.run();
                    };
                }};
            }};
        }};
    }
}
