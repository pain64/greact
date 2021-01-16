package greact.sample.plainjs.demo;

import com.greact.model.JSExpression;
import com.greact.model.async;
import com.over64.greact.dom.HTMLNativeElements.*;

import java.util.function.Consumer;

public class Grid<T> implements Component0<div> {
    @FunctionalInterface public interface DataProvider<T> {
        void onData(T[] data);
    }
    public interface Searcher<T> extends Component1<div, DataProvider<T>> {
    }

    Searcher<T> data;
    Column<T>[] columns = (Column<T>[]) new Object[0];
    Component1<div, T> selectedRow;

//    public Grid(T[] list) {
//        this.list = list;
//        // FIXME: нужно разобраться с VarArgs
//        //this.columns = JSExpression.of("[].slice.call(arguments, 1)");
//    }
    void log(Object obj) {
        JSExpression.of("console.log(obj)");
    }

    boolean strictEqual(Object lhs, Object rhs) {
        return JSExpression.of("lhs === rhs");
    }

    private T[] list = JSExpression.of("[]");
    private boolean rerenderAll;
    private T current = null;

    @Override
    public div mount() {
        return new div() {{
            new div() {{
                dependsOn = rerenderAll;
                new slot<>(data, fetched -> effect(list = fetched));
                new table() {{
                    className = "table table-hover table-striped";
                    new thead() {{
                        for (var col : columns) new td(col.header);
                    }};
                    new tbody() {{
                        for (var row : list)
                            new tr() {{
                                style.cursor = "pointer";
                                //className = row == current ? "table-active" : "";
                                onclick = ev -> {
                                    //FIXME: эффективнее сделать через ev.target.toggleClass
                                    if(current == row) current = null;
                                    else current = row;
                                    effect(rerenderAll);
                                    //effect(list);
                                };
                                if (row == current)
                                    new td() {{
                                        colSpan = columns.length;
                                        style.backgroundColor = "#fff9ad82";
                                        new table() {{
                                            className = "table";
                                            new tr() {{
                                                for (var col : columns)
                                                    new td(col.rowData.fetch(row).toString());
                                            }};
                                        }};
                                        new div() {{
                                            style.display = "flex";
                                            style.justifyContent = "center";
                                            new slot<>(selectedRow, current);
                                        }};
                                    }};
                                else
                                    for (var col : columns)
                                        new td(col.rowData.fetch(row).toString());
                            }};
                    }};
                }};
            }};
        }};
    }
}