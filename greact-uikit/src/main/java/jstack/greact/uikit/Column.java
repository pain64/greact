package jstack.greact.uikit;

import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.MemberRef;
import jstack.greact.uikit.controls.*;
import jstack.greact.dom.HTMLNativeElements;

import java.sql.Date;
import java.util.function.BiFunction;

public class Column<T, U> {
    public static final HTMLNativeElements.Component1<HTMLNativeElements.td, String> TEXT_AT_LEFT = value ->
        new HTMLNativeElements.td(value == null ? "" : value) {{
            style.textAlign = "left";
        }};

    static <T> boolean isNullOrUndefined(T value) {
        return JSExpression.of("typeof value === 'undefined' || value === null");
    }

    public String header;
    public String[] memberNames;
    public boolean hidden = false;
    public Control<U> editor = null;
    public BiFunction<U, T, String> backgroundColor = null;
    public HTMLNativeElements.Component2<HTMLNativeElements.td, U, T> view = (value, row) ->
        new HTMLNativeElements.td(isNullOrUndefined(value) ? "" : value.toString());

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
        columnName = JSExpression.of("columnName.replace('_', ' ')");
        columnName = JSExpression.of("columnName.charAt(0).toUpperCase() + columnName.slice(1)");
        this.header = columnName;
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

        this.view = switch (className) {
            case "java.util.Date" -> (d, row) -> new HTMLNativeElements.td(d == null ? "" : Dates.toLocaleDateString((Date) d));
            case "boolean", "java.lang.Boolean" -> (d, row) -> new HTMLNativeElements.td() {{
                // FIXME: fix css - CheckBox и нативные чекбоксы должны выглядеть одинаково?
                new HTMLNativeElements.input() {{
                    type = "checkbox";
                    checked = (boolean) d;
                    readOnly = true;
                }};
            }};
            default -> view;
        };

        this.editor = (Control<U>) editor;
    }

    public Column(MemberRef<T, U> ref) {
        applyDefaultSettings(ref.memberNames(), ref.className());
    }

    public Column<T, U> editor(Control<U> editor) {
        this.editor = editor;
        return this;
    }

    public Column<T, U> name(String theHeader) {
        this.header = theHeader;
        return this;
    }

    public Column<T, U> view(Mapper<U, String> mapper) {
        this.view = (value, row) -> new HTMLNativeElements.td(mapper.map(value));
        return this;
    }

    public Column<T, U> viewCell(HTMLNativeElements.Component1<HTMLNativeElements.td, U> newView) {
        @SuppressWarnings("unchecked")
        var casted = (HTMLNativeElements.Component2<HTMLNativeElements.td, U, T>) newView;
        this.view = casted;
        return this;
    }

    public Column<T, U> viewCell(HTMLNativeElements.Component2<HTMLNativeElements.td, U, T> newView) {
        this.view = newView;
        return this;
    }

    public Column<T, U> noedit() {
        this.editor = null;
        return this;
    }

    public Column<T, U> hide() {
        this.hidden = true;
        return this;
    }

    public Column<T, U> show() {
        this.hidden = false;
        return this;
    }
}
