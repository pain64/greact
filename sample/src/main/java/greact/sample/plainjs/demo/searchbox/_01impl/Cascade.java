package greact.sample.plainjs.demo.searchbox._01impl;

import com.over64.greact.dom.HTMLNativeElements.*;
import greact.sample.plainjs.demo.searchbox._00base._00Control;

public class Cascade<T> extends _00Control<T> {
    //FIXME: duplicate with SearchBox
    @FunctionalInterface
    public interface Func1<U1, T> {
        T apply(U1 u1);
    }

    _00Control in1;
    _00Control<T> in2;

    @Override
    public _00Control child() { return in2; }

    public <U1> Cascade(_00Control<U1> in1, Func1<U1, _00Control<T>> cascade1) {
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

    @Override
    public div mount() {
        return new div() {{
            new slot<div>(in1);
        }};
    }
}
