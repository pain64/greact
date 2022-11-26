package jstack.greact.uikit.controls;

import jstack.greact.dom.HTMLNativeElements;

public class Cascade<T> extends Control<T> {
    //FIXME: duplicate with SearchBox
    @FunctionalInterface public interface Func1<U1, T> {
        T apply(U1 u1);
    }

    Control in1;
    Control<T> in2;

    @Override
    public Control child() { return in2; }

    public <U1> Cascade(Control<U1> in1, Func1<U1, Control<T>> cascade1) {
        this.in1 = in1;
        this.in1.onReadyChanged = () -> {
            in2 = cascade1.apply(in1.value);
            in2.onReadyChanged = () -> {
                this.value = in2.value;
                this.ready = in2.ready;
                this.onReadyChanged.run();
            };

            this.onReadyChanged.run();
        };
    }

    @Override public HTMLNativeElements.div mount() {
        return new HTMLNativeElements.div() {{
            new HTMLNativeElements.slot<HTMLNativeElements.div>(in1);
        }};
    }
}
