package com.over64.greact.uikit;

import com.greact.model.ClassRef;
import com.greact.model.ClassRef.Reflexive;
import com.greact.model.JSExpression;
import com.greact.model.MemberRef;
import com.over64.greact.dom.HTMLNativeElements.*;

public class Grid<T> extends GridConfig2<T> implements Component0<div> {
    // FIXME: make strict equals by compiler default
    static <A> boolean strictEqual(A lhs, A rhs) { return JSExpression.of("lhs === rhs"); }

    public <U> Column<T, U> adjust(MemberRef<T, U> ref) {
        for (var col : columns) {
            if (col.memberNames.length != ref.memberNames().length) continue;
            for (var i = 0; i < col.memberNames.length; i++) {
                if (col.memberNames[i] != ref.memberNames()[i]) continue;
                return (Column<T, U>) col;
            }
        }

        return null; /* unreachable */
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
        _col._editor.value = JSExpression.of("this.fetchValue(rowData, col.memberNames)");
    }

    static <T> String colViewAsString(Column<T, ?> col, T rowData) {
        var colValue = JSExpression.of("this.fetchValue(rowData, col.memberNames)");
        if (colValue != null && col.viewMapper != null) {
            @SuppressWarnings("unchecked")
            var mapper = (Column.Mapper<Object, String>) col.viewMapper;
            return mapper.map(colValue);
        }

        return colValue != null ? colValue.toString() : "";
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
            new GridFilter<>(data, conf, rowData ->
                effect(selectedRowData = rowData));

            new div() {{ /* redraw point */
                if (selectedRow != null && selectedRowData != null) {
                    style.backgroundColor = "white";
                    style.display = "flex";
                    style.padding = "15px";
                    style.justifyContent = "center";
                    new div() {{
                        style.width = "100%";
                        new slot<>(selectedRow, selectedRowData);
                    }};
                }
            }};
        }};
    }
}