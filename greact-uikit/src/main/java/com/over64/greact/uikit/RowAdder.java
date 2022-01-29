package com.over64.greact.uikit;

import com.greact.model.JSExpression;
import com.over64.greact.uikit.controls.Control;

public class RowAdder<T> {
    Control<?>[] controls;
    SearchBox.AsyncSupplier<T> loader;

    void nativeInit(/* arguments... */) {
        this.controls = JSExpression.of("[].slice.call(arguments, 1, arguments.length - 1)");
        //for (var control : controls) control.onReadyChanged = this::onReadyChanged;
        this.loader = () -> JSExpression.of("arguments[arguments.length - 1](...this.controls.map(c => c.value))");
        //calcChildren();
    }

    public <U1> RowAdder(Control<U1> in1, SearchBox.AsyncFunc1<U1, T> loader) {
        JSExpression.of("this._nativeInit(...arguments)");
    }

    public <U1, U2> RowAdder(Control<U1> in1, Control<U2> in2, SearchBox.AsyncFunc2<U1, U2, T> loader) {
        JSExpression.of("this._nativeInit(...arguments)");
    }

    public <U1, U2, U3> RowAdder(Control<U1> in1, Control<U2> in2, Control<U3> in3, SearchBox.AsyncFunc3<U1, U2, U3, T> loader) {
        JSExpression.of("this._nativeInit(...arguments)");
    }
}
