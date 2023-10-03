package jstack.greact.uikit;

import jstack.greact.html.Component;
import jstack.greact.html.Component0;
import jstack.greact.html.div;
import jstack.greact.html.slot;
import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.Require;
import jstack.jscripter.transpiler.model.Async;
import jstack.greact.uikit.controls.Control;

@Require.CSS("search_box.css")
public class SearchBox implements Component0<div> {
    public String controlsClassName = null;
    Control[] controls;
    Control[] controlsWithChildren;
    boolean canSearch = false;
    boolean doSearch = false;
    AsyncSupplier<Component0<div>> loader;
    Component0<div> loaded;

    @FunctionalInterface public interface AsyncSupplier<T> {
        @Async T load();
    }

    @FunctionalInterface public interface AsyncFunc1<A1, R> {
        @Async R load(A1 a1);
    }

    @FunctionalInterface public interface AsyncFunc2<A1, A2, R> {
        @Async R load(A1 a1, A2 a2);
    }

    @FunctionalInterface public interface AsyncFunc3<A1, A2, A3, R> {
        @Async R load(A1 a1, A2 a2, A3 a3);
    }

    @FunctionalInterface public interface AsyncFunc4<A1, A2, A3, A4, R> {
        @Async R load(A1 a1, A2 a2, A3 a3, A4 a4);
    }

    <V> void push(V[] array, V value) {
        JSExpression.of(":1.push(:2)", array, value);
    }

    void pushRec(Control[] dest, Control control) {
        push(dest, control);
        if (control.child() != null)
            pushRec(dest, control.child());
    }

    void calcChildren() {
        controlsWithChildren = new Control[]{};
        for (var control : controls)
            pushRec(controlsWithChildren, control);
    }

    void checkCanSearch() {
        canSearch = true;
        for (var control : controls)
            if (!control.ready) {
                canSearch = false;
                break;
            }
    }

    void onReadyChanged() {
        checkCanSearch();
        calcChildren();
        JSExpression.of("this._performChangedEffects()");
    }

    @Async void performChangedEffects() {
        if (canSearch)
            loaded = loader.load();

        effect(controlsWithChildren, canSearch);
    }

    void nativeInit(/* arguments... */) {
        this.controls = JSExpression.of("[].slice.call(arguments, 1, arguments.length - 1)");
        for (var control : controls) control.onReadyChanged = this::onReadyChanged;
        this.loader = () -> JSExpression.of("arguments[arguments.length - 1](...this.controls.map(c => c.value))");
        calcChildren();
    }

    public <U1> SearchBox(Control<U1> in1, AsyncFunc1<U1, Component0<div>> view) {
        JSExpression.of("this._nativeInit(...arguments)");
    }

    public <U1, U2> SearchBox(Control<U1> in1, Control<U2> in2, AsyncFunc2<U1, U2, Component<div>> view) {
        JSExpression.of("this._nativeInit(...arguments)");
    }

    public <U1, U2, U3> SearchBox(Control<U1> in1, Control<U2> in2, Control<U3> in3, AsyncFunc3<U1, U2, U3, Component0<div>> view) {
        JSExpression.of("this._nativeInit(...arguments)");
    }

    public <U1, U2, U3, U4> SearchBox(Control<U1> in1, Control<U2> in2, Control<U3> in3, Control<U4> in4, AsyncFunc4<U1, U2, U3, U4, Component0<div>> view) {
        JSExpression.of("this._nativeInit(...arguments)");
    }

    @Override @Async public div mount() {
        checkCanSearch();
        if (canSearch) {
            loaded = loader.load();
            doSearch = true;
        }

        return new div() {{
            new div() {{
                className = "search-box";
                if (controlsClassName != null)
                    className += " " + controlsClassName;

                for (var control : controlsWithChildren)
                    new slot<div>(control);

                /* FIXME: make autosearch */

//                new button("искать") {{
//                    style.margin = "2px";
//                    className = canSearch ? "search-button active" : " search-button disabled";
//                    onclick = ev -> {
//                        loaded = loader.load();
//                        effect(doSearch = true);
//                    };
//                }};
            }};

            if (canSearch) {
                // doSearch = false;
                new slot<>(loaded);
            }
        }};
    }
}
