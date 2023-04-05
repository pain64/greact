package jstack.greact.uikit.controls;

import jstack.greact.dom.HTMLNativeElements.div;
import jstack.greact.dom.HTMLNativeElements.slot;
import jstack.greact.uikit.Promise;
import jstack.jscripter.transpiler.model.async;

public class Cascade<T> extends Control<T> {
    //FIXME: duplicate with SearchBox
    @FunctionalInterface public interface Func1<U1, T> {
        @async T apply(U1 u1);
    }

    Control in1;
    Control<T> in2;

    @Override
    public Control child() { return in2; }

    public <U1> Cascade(Control<U1> in1, Func1<U1, Control<T>> cascade1) {
        this.in1 = in1;
        new Promise<Control<T>>((resolve, reject) -> {
            resolve.run(cascade1.apply(in1.value));
        }).then((res) -> {
            this.in1.onReadyChanged = () -> {
                in2 = res;
                in2.onReadyChanged = () -> {
                    this.value = in2.value;
                    this.ready = in2.ready;
                    this.onReadyChanged.run();
                };
                this.onReadyChanged.run();
            };
        });
    }

    @Override public div mount() {
        return new div() {{
            new slot<div>(in1);
        }};
    }
}
