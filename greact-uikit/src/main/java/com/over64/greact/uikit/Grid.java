package com.over64.greact.uikit;

import com.greact.model.Require;
import com.greact.model.ClassRef;
import com.greact.model.ClassRef.Reflexive;
import com.greact.model.JSExpression;
import com.greact.model.MemberRef;
import com.over64.greact.dom.HTMLNativeElements.Component0;
import com.over64.greact.dom.HTMLNativeElements.div;
import com.over64.greact.dom.HTMLNativeElements.slot;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Require.CSS("grid.css")
public class Grid<T> extends GridConfig2<T> implements Component0<div> {
    // FIXME: make strict equals by compiler default
    static <A> boolean strictEqual(A lhs, A rhs) {return JSExpression.of("lhs === rhs");}

    public static class Adjuster<T> {
        final Column<T, ?>[] columns;
        public Adjuster(Column<T, ?>[] columns) {this.columns = columns;}

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
        // FIXME: varargs
        refs = JSExpression.of("arguments");
        Column<T, ?>[] dest = new Column[]{};
        for (var ref : refs)
            Array.push(dest, adjust(ref));

        return new Adjuster<>(dest);
    }

    public Adjuster<T> adjustRange(MemberRef<T, ?> from, MemberRef<T, ?> to) {
        Column<T, ?>[] dest = new Column[]{};
        for(var i = colIndex(from); i <= colIndex(to); i++)
            Array.push(dest, columns[i]);

        return new Adjuster<>(dest);
    }

    // BEGIN move to column
    static <T> Object fetchValue(T rowData, String[] memberNames) {
        Object acc = rowData;
        for (var i = 0; i < memberNames.length; i++)
            acc = JSExpression.of("acc[memberNames[i]]");
        return acc;
    }

    static <T> void setEditorValueFromRowValue(Column<T, ?> col, T rowData) {
        @SuppressWarnings("unchecked")
        var _col = (Column<T, Object>) col;
        _col.editor.value = JSExpression.of("this._fetchValue(rowData, col.memberNames)");
    }

    static <T> void setValue(T rowData, String[] memberNames, Object value) {
        Object acc = rowData;
        for (var i = 0; i < memberNames.length - 1; i++) {
            acc = JSExpression.of("acc[memberNames[i]]");
            acc = JSExpression.of("typeof acc === 'undefined' ? {} : acc");
        }

        JSExpression.of("acc[memberNames[memberNames.length - 1]] = value");
    }
    // END move to column

    final T[] data;
    T selectedRowData;
    public Grid(@Reflexive T[] data) {
        this.data = data;
        initColumnsByDefault(data);
    }

    void initColumnsByDefault(T[] data) {
        var gridDataClass = ClassRef.of(data).params()[0];
        this.columns = Array.map(gridDataClass.fields(), fieldRef -> {
            var col = new Column<T, Object>();
            col.applyDefaultSettings(new String[]{fieldRef.name()}, fieldRef.__class__().name());
            return col;
        });
    }


    @Override public div mount() {
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