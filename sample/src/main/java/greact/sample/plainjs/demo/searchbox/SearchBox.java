package greact.sample.plainjs.demo.searchbox;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.*;
import greact.sample.plainjs.demo.searchbox._00base._00Control;

public class SearchBox<T> implements Component0<div> {
    _00Control[] controls;
    _00Control[] controlsWithChildren;
    Component0<div> onData;
    boolean canSearch = false;
    boolean doSearch = false;

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
        this.onData = JSExpression.of("arguments[arguments.length - 1]");
        calcChildren();
    }

    public <U1> SearchBox(_00Control<U1> in1, Component1<div, U1> slot) {
        JSExpression.of("this.nativeInit(...arguments)");
    }

    public <U1, U2> SearchBox(_00Control<U1> in1, _00Control<U2> in2, Component2<div, U1, U2> slot) {
        JSExpression.of("this.nativeInit(...arguments)");
    }

    public <U1, U2, U3> SearchBox(_00Control<U1> in1, _00Control<U2> in2, _00Control<U3> in3, Component3<div, U1, U2, U3> slot) {
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
                    onclick = ev -> effect(doSearch = true);
                }};
            }};

            if (doSearch) {
                doSearch = false;
                switch (controls.length) {
                    case 0: new slot<>(onData); break;
                    case 1: new slot<>((Component1<div, Object>) onData, controls[0].value); break;
                    case 2: new slot<>((Component2<div, Object, Object>) onData, controls[0].value, controls[1].value); break;
                    case 3: new slot<>((Component3<div, Object, Object, Object>) onData, controls[0].value, controls[1].value, controls[2].value); break;
                }
            }
        }};
    }
}
