package jstack.greact.uikit;

import jstack.greact.html.Component1;
import jstack.greact.html.Component2;
import jstack.greact.html.input;
import jstack.greact.html.td;
import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.MemberRef;
import jstack.greact.uikit.controls.*;

import java.sql.Date;
import java.util.function.BiFunction;

public class Column<T, U> {
    public static final Component1<td, String> TEXT_AT_LEFT = value ->
        new td(value == null ? "" : value) {{
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
    public Component2<td, U, T> view = (value, row) ->
        new td(isNullOrUndefined(value) ? "" : value.toString());

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
        var regex1 = "(\\\\p{Ll}\\\\p{Lu})";
        var regex2 = "(\\\\$\\\\d+)";
        var regexMode = "gu";

        columnName = JSExpression.of(
            "columnName.replaceAll(new RegExp(regex1, regexMode), function(match, bg) {" +
                "return bg[0] + ' ' + bg[1].toLowerCase();" +
                "})"
        );

        columnName = JSExpression.of(
            "columnName.replaceAll(new RegExp(regex2, regexMode), function(match, bg) {" +
                "return String.fromCharCode(parseInt(bg.slice(1)));" +
                "})"
        );

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
            case "java.util.Date", "java.sql.Date" ->
                (d, row) -> new td(d == null ? "" : Dates.toLocaleDateString((Date) d));
            case "boolean", "java.lang.Boolean" -> (d, row) -> new td() {{
                // FIXME: fix css - CheckBox и нативные чекбоксы должны выглядеть одинаково?
                new input() {{
                    className = "form-check-input";
                    style.verticalAlign = "middle";
                    type = "checkbox";
                    checked = (boolean) d;
                    readOnly = true;
                }};
            }};
            default -> view;
        };

        this.editor = (Control<U>) editor;
    }

    public Column(String name, MemberRef<T, U> ref) {
        applyDefaultSettings(ref.memberNames(), ref.className());
        this.header = name;
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
        this.view = (value, row) -> new td(mapper.map(value));
        return this;
    }

    public Column<T, U> viewCell(Component1<td, U> newView) {
        @SuppressWarnings("unchecked")
        var casted = (Component2<td, U, T>) newView;
        this.view = casted;
        return this;
    }

    public Column<T, U> viewCell(Component2<td, U, T> newView) {
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
