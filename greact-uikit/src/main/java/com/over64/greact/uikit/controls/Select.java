package com.over64.greact.uikit.controls;

import com.greact.model.JSExpression;
import com.greact.model.MemberRef;
import com.over64.greact.dom.HTMLNativeElements.*;

public class Select<T> extends Control<T> {
    @FunctionalInterface public interface Mapper<V, U> {
        U map(V kv);
    }

    final Indexed<T>[] variants;
    int valueIdx;

    // FIXME почини, наконец, перегруженные конструкторы!
    public static <A> MemberRef<A, A> identity() {
        return JSExpression.of("{value: (v) => v}");
    }

    // FIXME: починить перегруженные конструкторы с разным количеством аргументов
//    public Select(T[] options) {
//        // FIXME: call another constructor
//        // this(variants, v -> v, c -> c.toString());
//        this.variants = new Indexed[options.length];
//        for (var i = 0; i < variants.length; i++)
//            this.variants[i] = new Indexed<>(i, options[i], options[i].toString());
//    }

    // FIXME: Использовать здесь MemberRef нет необходимости, однако, пока не транспилятся
    //  inner классы и record components
    public <V> Select(V[] options, MemberRef<V, T> values, MemberRef<V, String> captions) {
        this.variants = new Indexed[options.length];
        for (var i = 0; i < options.length; i++)
            this.variants[i] = new Indexed<>(i, values.value(options[i]), captions.value(options[i]));
        if(this.variants.length != 0)
            this.value = this.variants[0].element;
    }

    public Select<T> label(String lbl) {
        this._label = lbl;
        return this;
    }

    @Override public Control child() { return null; }

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
                style.display = "flex";
                style.alignItems = "center";
                style.whiteSpace = "nowrap";

                new span(_label) {{
                 //   style.margin = "0px 5px 0px 0px";
                }};

                new select() {{
                    style.width = "100%";
                    className = "form-control";
                    for (var variant : variants)
                        new option(variant.caption) {{
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

    public Select<T> slots(int n) {
        _slots = n;
        return this;
    }
}
