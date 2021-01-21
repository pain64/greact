package greact.sample.plainjs.demo.searchbox;

import com.greact.model.JSExpression;
import com.greact.model.async;
import com.over64.greact.dom.HTMLNativeElements.*;
import greact.sample.plainjs.demo.Grid;
import greact.sample.plainjs.demo.searchbox._00base._00Control;

public class SearchBox<T> implements Grid.Searcher<T> {

    @FunctionalInterface
    public interface Provider1<U1, T> {
        @async
        T[] provide(U1 u1);
    }

    @FunctionalInterface
    public interface Provider2<U1, U2, T> {
        @async
        T[] provide(U1 u1, U2 u2);
    }

    @FunctionalInterface
    public interface Provider3<U1, U2, U3, T> {
        @async
        T[] provide(U1 u1, U2 u2, U3 u3);
    }

    @FunctionalInterface
    interface AsyncSupplier<T> {
        @async
        T supply();
    }

    _00Control[] controls;
    _00Control[] controlsWithChildren;
    AsyncSupplier<T[]> loader;
    boolean canSearch = false;

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

    void onReadyChanged() {
        canSearch = true;
        for (var control : controls)
            if (!control.ready) {
                canSearch = false;
                break;
            }
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

    public <U1> SearchBox(_00Control<U1> in1, Provider1<U1, T> query) {
        JSExpression.of("this.nativeInit(...arguments)");
    }

    public <U1, U2> SearchBox(_00Control<U1> in1, _00Control<U2> in2, Provider2<U1, U2, T> query) {
        JSExpression.of("this.nativeInit(...arguments)");
    }

    public <U1, U2, U3> SearchBox(_00Control<U1> in1, _00Control<U2> in2, _00Control<U3> in3, Provider3<U1, U2, U3,
        T> query) {
        JSExpression.of("this.nativeInit(...arguments)");
    }

    @Override
    public div mount(Grid.DataProvider<T> provider) {
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
            style.display = "flex";
            style.justifyContent = "center";
            style.flexWrap = "wrap";
            style.alignItems = "center";

            for (var control : controlsWithChildren)
                new slot<div>(control);
            new button("искать") {{
                style.margin = "2px";
                className = canSearch ? "search-button active" : " search-button disabled";
                onclick = ev -> provider.onData(loader.supply());
            }};
        }};
    }
}
