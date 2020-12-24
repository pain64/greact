package greact.sample.plainjs.demo;

import com.greact.model.JSExpression;
import com.over64.greact.dom.HTMLNativeElements.*;

import java.util.function.Consumer;

public class Grid<T> implements Component0<div> {
    private final T[] list;
    Column<T>[] columns = (Column<T>[]) new Object[0];
    Component1<div, T> selectedRow;

    public Grid(T[] list) {
        this.list = list;
        // FIXME: нужно разобраться с VarArgs
        //this.columns = JSExpression.of("[].slice.call(arguments, 1)");
    }

    void log(Object obj) {
        JSExpression.of("console.log(obj)");
    }

    private T current = null;

    @Override
    public div mount() {
        return new div() {{
            new table() {{
                className = "table table-striped";
                new tbody() {{
                    for (var row : list) new tr() {{
                        style.cursor = "pointer";
                        onclick = ev -> effect(current = row);
                        for (var col : columns) new td(
                            col.rowData.fetch(row).toString());
                    }};
                }};
            }};

            if(current != null)
                new slot<>(selectedRow, current);
            else
                new h2("No row selected");
        }};
    }
}