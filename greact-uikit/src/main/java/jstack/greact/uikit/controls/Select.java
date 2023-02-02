package jstack.greact.uikit.controls;

import jstack.greact.dom.HTMLNativeElements.*;
import jstack.greact.uikit.GridConfig2;
import jstack.jscripter.transpiler.model.Require;
import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.MemberRef;

@Require.CSS("select.css")
public class Select<T> extends Control<T> {
    @FunctionalInterface public interface Mapper<V, U> {
        U map(V kv);
    }

    Indexed<T>[] variants;
    int valueIdx;


    public Select(T[] options) {
        this(options,
            JSExpression.of("{_value: (v) => v}"),
            JSExpression.of("{_value: (v) => v.toString()}"));
    }

    // FIXME: Использовать здесь MemberRef нет необходимости, однако, пока не транспилятся
    //  inner классы и record components
    public Select(T[] options, MemberRef<T, String> captions) {
        this(options, JSExpression.of("{_value: (v) => v}"), captions);
    }

    public <V> Select(V[] options, MemberRef<V, T> values, MemberRef<V, String> captions) {
        this.variants = new Indexed[options.length];
        for (var i = 0; i < options.length; i++)
            this.variants[i] = new Indexed<>(i, values.value(options[i]), captions.value(options[i]));

        if (this.variants.length != 0) {
            this.value = this.variants[0].element;
            this.ready = true;
            this.onReadyChanged.run();
        }
    }

    public Select<T> label(String lbl) {
        this.label = lbl;
        return this;
    }

    public GridConfig2.AsyncHandler<T> onchange = null;

    @Override public Control<?> child() { return null; }

    @Override
    public div mount() {
        var self = this;
//        if(self.value != null && self.variants.length > 0) {
//            self.value = variants[0].element;
//            self.onReadyChanged.run();
//        }

        return new div() {{
            //  style.padding = "0px 2px";
            //   style.margin = "0px 10px";

            new label() {{
                className = "select";

                new span(label) {{
                    className = "select-span";
                }};

                new select() {{
                    id = "select-body";
                    className = "form-control";
                    for (var variant : variants)
                        new option(variant.caption) {{
                            selected = self.value == variant.element;
                            value = "" + variant.i;
                        }};
                    onchange = ev -> {
                        self.valueIdx = JSExpression.of("parseInt(ev.target.value)");
                        self.value = variants[self.valueIdx].element;
                        if (self.onchange != null)
                            self.onchange.handle(self.value);
                        self.ready = true;
                        self.onReadyChanged.run();
                    };
                }};
            }};
        }};
    }

    public Select<T> slots(int n) {
        slots = n;
        return this;
    }
}
