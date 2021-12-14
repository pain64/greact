package com.over64.greact.uikit;

import com.greact.model.JSExpression;
import com.greact.model.MemberRef;
import com.over64.greact.dom.HTMLNativeElements.input;
import com.over64.greact.dom.HTMLNativeElements.Component1;
import com.over64.greact.dom.HTMLNativeElements.td;
import com.over64.greact.uikit.controls.*;

import java.sql.Date;

public class Column<T, U> {
    public static final Component1<td, String> TEXT_AT_LEFT = value ->
        new td(value == null ? "" : value) {{
            style.textAlign = "left";
        }};

    public String _header;
    public String[] memberNames;
    public Control<U> _editor = null;
    Component1<td, U> _view = value -> new td(value == null ? "" : value.toString());

    @FunctionalInterface public interface Mapper<V, U> {
        U map(V kv);
    }

    public Column() {
        // FIXME: cannot ref to another constructor like:
        // this(args)
        // applyDefaultSettings must be another constructor
    }

    @SuppressWarnings("unchecked")
    void applyDefaultSettings(String[] memberNames, String className) {
        var columnName = memberNames[memberNames.length - 1];
        JSExpression.of("columnName = columnName.replace('_', ' ')");
        JSExpression.of("columnName = columnName.charAt(0).toUpperCase() + columnName.slice(1)");
        this._header = columnName;
        this.memberNames = memberNames;

        var editor = switch (className) {
            case "boolean", "java.lang.Boolean" -> new CheckBox();
            case "long", "java.lang.Long" -> new LongInput();
            case "int", "java.lang.Integer" -> new IntInput();
            case "java.lang.String" -> new StrInput();
            case "java.math.BigDecimal" -> new FloatInput();
            case "java.util.Date" -> new DateInput();
            default -> null;
        };

        this._view = switch (className) {
            case "java.util.Date" -> d -> new td(d == null ? "" : Dates.toLocaleDateString((Date) d));
            case "boolean", "java.lang.Boolean" -> d -> new td() {{
                // FIXME: fix css - CheckBox и нативные чекбоксы должны выглядеть одинаково?
                new input() {{
                    type = "checkbox";
                    checked = (boolean) d;
                    readOnly = true;
                }};
            }};
            default -> _view;
        };

        this._editor = (Control<U>) editor;
    }

    public Column(MemberRef<T, U> ref) {
        applyDefaultSettings(ref.memberNames(), ref.className());
    }

    public Column<T, U> editor(Control<U> editor) {
        this._editor = editor;
        return this;
    }

    public Column<T, U> name(String theHeader) {
        this._header = theHeader;
        return this;
    }

    public Column<T, U> view(Mapper<U, String> mapper) {
        this._view = value -> new td(mapper.map(value));
        return this;
    }

    public Column<T, U> viewCell(Component1<td, U> newView) {
        this._view = newView;
        return this;
    }

    public Column<T, U> noedit() {
        this._editor = null;
        return this;
    }
}
