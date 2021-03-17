package greact.sample.plainjs.demo.searchbox._01impl;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.*;
import greact.sample.plainjs.demo.searchbox._00base.Indexed;
import greact.sample.plainjs.demo.searchbox._00base._00Control;

public class Select<T> extends _00Control<T> {

    final Indexed<T>[] variants;
    int valueIdx;

    public Select(T[] variants) {
        this.variants = new Indexed[variants.length];
        for (var i = 0; i < variants.length; i++)
            this.variants[i] = new Indexed<>(i, variants[i]);
    }

    public Select<T> label(String lbl) {
        this._label = lbl;
        return this;
    }

    @Override public _00Control child() { return null; }

    @Override
    public div mount() {
        var self = this;

        return new div() {{
          //  style.padding = "0px 2px";
         //   style.margin = "0px 10px";

            new label() {{
                new span(_label) {{
                 //   style.margin = "0px 5px 0px 0px";
                }};

                new select() {{
                    style.width = "100%";
                    className = "form-control";
                    for (var variant : variants)
                        new option(variant.element.toString()) {{
                            selected = self.value == variant.element;
                            value = "" + variant.i;
                        }};
                    onchange = ev -> {
                        self.valueIdx = JSExpression.of("parseInt(ev.target.value)");
                        self.value = variants[self.valueIdx].element;
                        self.ready = true;
                        self.onReadyChanged.run();
                    };
                }};
            }};
        }};
    }
}
