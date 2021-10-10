package com.over64.greact.uikit;

import com.greact.model.JSExpression;
import com.greact.model.MemberRef;
import com.over64.greact.uikit.controls.Control;
import com.over64.greact.uikit.controls.IntInput;
import com.over64.greact.uikit.controls.LongInput;
import com.over64.greact.uikit.controls.StrInput;

import java.sql.Date;

public class Column<T, U> {
    public String _header;
    public String[] memberNames;
    public Mapper<U, String> viewMapper = null;
    public Control<U> _editor = null;

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
            case "long", "java.lang.Long" -> new LongInput();
            case "int", "java.lang.Integer" -> new IntInput();
            case "java.lang.String" -> new StrInput();
            default -> null;
        };
        Mapper<?, String> viewMapper = switch (className) {
            case "java.sql.Date" -> d -> Dates.toLocaleDateString((Date) d);
            default -> null;
        };

        this._editor = (Control<U>) editor;
        this.viewMapper = (Mapper<U, String>) viewMapper;
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
        this.viewMapper = mapper;
        return this;
    }

    public Column<T, U> noedit() {
        this._editor = null;
        return this;
    }
}
