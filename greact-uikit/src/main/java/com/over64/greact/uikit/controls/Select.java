package com.over64.greact.uikit.controls;

import com.greact.model.JSExpression;
import com.greact.model.MemberRef;
import com.over64.greact.dom.HTMLNativeElements.*;

import java.util.function.Function;

public class Select<T> extends Control<T> {
    @FunctionalInterface public interface Mapper<V, U> {
        U map(V kv);
    }

    // FIXME: Использовать здесь MemberRef нет необходимости, однако, пока не транспилятся
    //  inner классы и record components
    public static <A> MemberRef<A, A> identity() {
        return JSExpression.of("{value: (v) => v}");
    }

    public static <A> MemberRef<A, String> identityToString() {
        return JSExpression.of("{value: (v) => v.toString()}");
    }

    Indexed<T>[] variants;
    int valueIdx;

    public Select(T[] options) {
        constructorShared(options, identity(), identityToString());
    }

    public Select(T[] options, MemberRef<T, String> captions) {
        constructorShared(options, identity(), captions);
    }

    public <V> Select(V[] options, MemberRef<V, T> values, MemberRef<V, String> captions) {
        constructorShared(options, values, captions);
    }

    /* FIXME: починить вызов конструктора из конструктора */
    <V> void constructorShared(V[] options, MemberRef<V, T> values, MemberRef<V, String> captions) {
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
        this._label = lbl;
        return this;
    }

    @Override public Control child() {return null;}

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
                    style.margin = "0px 5px 0px 0px";
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
