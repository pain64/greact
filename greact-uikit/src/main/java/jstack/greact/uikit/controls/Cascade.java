package jstack.greact.uikit.controls;

import jstack.greact.html.div;
import jstack.greact.html.slot;
import jstack.greact.uikit.Promise;
import jstack.jscripter.transpiler.model.Async;

public class Cascade<T, U1> extends Control<T> {
    //FIXME: duplicate with SearchBox
    @FunctionalInterface public interface AsyncFunc<U1, T> {
        @Async
        T apply(U1 u1);
    }

    Control<U1> in1;
    Control<T> in2;
    AsyncFunc<U1, Control<T>> func;

    @Override
    public Control<T> child() { return in2; }

    public Cascade(Control<U1> in1, AsyncFunc<U1, Control<T>> func) {
        this.in1 = in1;
        this.func = func;

        new Promise<Control<T>>((resolve, reject) ->
            resolve.run(func.apply(in1.value))
        ).then((res) -> this.in1.onReadyChanged = () -> {
            in2 = res;
            in2.onReadyChanged = () -> {
                this.value = in2.value;
                this.ready = in2.ready;
                this.onReadyChanged.run();
            };
            this.onReadyChanged.run();
        });
    }

    @Override public div render() {
        // FIXME: render не вызывает func и как следствие не получает новые данные с сервера
        return new div() {{
            new slot<>(in1);
        }};
    }
}
