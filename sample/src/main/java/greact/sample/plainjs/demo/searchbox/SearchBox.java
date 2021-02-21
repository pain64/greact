package greact.sample.plainjs.demo.searchbox;

import com.greact.model.JSExpression;
import com.greact.model.async;
import com.over64.greact.dom.HTMLNativeElements.*;
import greact.sample.plainjs.demo.searchbox._00base._00Control;

public class SearchBox<T> implements Component0<div> {
    _00Control[] controls;
    _00Control[] controlsWithChildren;
    boolean canSearch = false;
    boolean doSearch = false;
    AsyncSupplier<T> loader;
    public Component1<div, T[]> view;
    T[] loaded;
    public Object dependsOn;

    @FunctionalInterface public interface AsyncSupplier<T> {
        @async T[] load();
    }
    @FunctionalInterface public interface AsyncFunc1<A1, R> {
        @async R load(A1 a1);
    }
    @FunctionalInterface public interface AsyncFunc2<A1, A2, R> {
        @async R load(A1 a1, A2 a2);
    }
    @FunctionalInterface public interface AsyncFunc3<A1, A2, A3, R> {
        @async R load(A1 a1, A2 a2, A3 a3);
    }

    <V> void push(V[] array, V value) {
        JSExpression.of("array.push(value)");
    }

    void pushRec(_00Control[] dest, _00Control control) {
        push(dest, control);
        if (control.child() != null)
            pushRec(dest, control.child());
    }

    void calcChildren() {
        controlsWithChildren = new _00Control[]{};
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
        effect(controlsWithChildren);
        effect(canSearch);
    }

    void nativeInit(/* arguments... */) {
        this.controls = JSExpression.of("[].slice.call(arguments, 1, arguments.length - 1)");
        for (var control : controls) control.onReadyChanged = this::onReadyChanged;
        this.loader = () -> JSExpression.of("arguments[arguments.length - 1](...this.controls.map(c => c.value))");
        calcChildren();
    }

    public <U1> SearchBox(_00Control<U1> in1, AsyncFunc1<U1, T[]> loader) {
        JSExpression.of("this.nativeInit(...arguments)");
    }

    public <U1, U2> SearchBox(_00Control<U1> in1, _00Control<U2> in2, AsyncFunc2<U1, U2, T[]> loader) {
        JSExpression.of("this.nativeInit(...arguments)");
    }

    public <U1, U2, U3> SearchBox(_00Control<U1> in1, _00Control<U2> in2, _00Control<U3> in3, AsyncFunc3<U1, U2, U3, T[]> loader) {
        JSExpression.of("this.nativeInit(...arguments)");
    }

    @Override public div mount() {
        checkCanSearch();
        if(canSearch) doSearch = true;

        return new div() {{
            new style("""
                .search-button {
                  background-color:black;
                  color: #fff;
                  border-radius:3px;
                  border: none;
                  height:24px;
                }
                .active {
                 cursor:pointer;
                }
                .disabled {
                  background-color: #cbc3c3;
                }
                """);
            new div() {{
                style.display = "flex";
                style.justifyContent = "center";
                style.flexWrap = "wrap";
                style.alignItems = "center";

                for (var control : controlsWithChildren)
                    new slot<div>(control);

                new button("искать") {{
                    style.margin = "2px";
                    className = canSearch ? "search-button active" : " search-button disabled";
                    onclick = ev -> {
                        loaded = loader.load();
                        effect(doSearch = true);
                    };
                }};
            }};

            if (doSearch) {
                doSearch = false;
                new slot<>(view, loaded);
            }
        }};
    }
}
