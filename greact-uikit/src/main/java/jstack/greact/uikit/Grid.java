package jstack.greact.uikit;

import jstack.greact.html.Component0;
import jstack.greact.html.div;
import jstack.greact.html.slot;
import jstack.greact.model.MemberRef;
import jstack.jscripter.transpiler.model.*;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Require.CSS("grid.css")
public class Grid<T> extends GridConfig2<T> implements Component0<div> {
    // FIXME: make strict equals by compiler default
    static <A> boolean strictEqual(A lhs, A rhs) { return JSExpression.of(":1 === :2", lhs, rhs); }

    public static class Adjuster<T> {
        final Column<T, ?>[] columns;
        public Adjuster(Column<T, ?>[] columns) { this.columns = columns; }

        public void each(Consumer<Column<T, ?>> action) {
            eachIndexed((__, col) -> action.accept(col));
        }

        public void eachIndexed(BiConsumer<Integer, Column<T, ?>> action) {
            for (var i = 0; i < columns.length; i++) action.accept(i, columns[i]);
        }
    }

    int colIndex(MemberRef<T, ?> ref) {
        for (var idx = 0; idx < columns.length; idx++) {
            var col = columns[idx];
            if (col.memberNames.length != ref.memberNames().length) continue;
            for (var i = 0; i < col.memberNames.length; i++) {
                // FIXME: https://trello.com/c/zJarOuQl/140-bug-equals
                if (col.memberNames[i] != ref.memberNames()[i]) continue;
                return idx;
            }
        }

        // FIXME: throw exception instead
        return -1; /* unreachable */
    }

    public <U> Column<T, U> adjust(MemberRef<T, U> ref) {
        @SuppressWarnings("unchecked")
        var col = (Column<T, U>) columns[colIndex(ref)];
        return col;
    }

    @SafeVarargs public final Adjuster<T> adjustMany(MemberRef<T, ?>... refs) {
        Column<T, ?>[] dest = new Column[]{};
        for (var ref : refs)
            Array.push(dest, adjust(ref));

        return new Adjuster<>(dest);
    }

    public Adjuster<T> adjustRange(MemberRef<T, ?> from, MemberRef<T, ?> to) {
        Column<T, ?>[] dest = new Column[]{};
        for (var i = colIndex(from); i <= colIndex(to); i++)
            Array.push(dest, columns[i]);

        return new Adjuster<>(dest);
    }

    // BEGIN move to column
    static <T> Object fetchValue(T rowData, String[] memberNames) {
        Object acc = rowData;
        for (var i = 0; i < memberNames.length; i++)
            acc = JSExpression.of(":2[:1[:3]]", memberNames, acc, i);
        return acc;
    }

    static <T> void setEditorValueFromRowValue(Column<T, ?> col, T rowData) {
        @SuppressWarnings("unchecked")
        var _col = (Column<T, Object>) col;
        var mn = col.memberNames;
        _col.editor.value = JSExpression.of("this._fetchValue(:2, :1)", mn, rowData);
    }

    static <T> void setValue(T rowData, String[] memberNames, Object value) {
        Object acc = rowData;
        for (var i = 0; i < memberNames.length - 1; i++) {
            acc = JSExpression.of(":1[:2[:3]]", acc, memberNames, i);
            acc = JSExpression.of("typeof :1 === 'undefined' ? {} : :1", acc);
        }

        JSExpression.of(":2[:1[:1.length - 1]] = value", memberNames, acc);
    }
    // END move to column

    public final T[] data;
    T selectedRowData;

    // Не компилируется с @DoNotTranspile
    public Grid(T[] data) {
        this.data = data;
    }

    public Grid(T[] data, Column<T, ?>[] columns) {
        this.data = data;
        this.columns = columns;
    }

    @Override public div render() {
        var conf = (GridConfig2<T>) this;

        return new div() {{
            className = "grid-main";

            new GridFilter<>(data, conf, rowData ->
                effect(selectedRowData = rowData));

            new div() {{ /* redraw point */
                if (selectedRow != null && selectedRowData != null) {
                    className = "grid";
                    new div() {{
                        className = "grid-body";
                        new slot<>(selectedRow, selectedRowData);
                    }};
                }
            }};
        }};
    }
}