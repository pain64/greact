package jstack.greact.uikit;

import jstack.greact.dom.HTMLNativeElements.*;
import jstack.jscripter.transpiler.model.async;

public class GridConfig2<T> {
    @FunctionalInterface public interface AsyncHandler<T> {
        @async void handle(T value);
    }
    @FunctionalInterface public interface AsyncSupplier<T> {
        @async T supply(T value);
    }

    public Component1<div, Component0<table>> pageView =
        gridTable -> new div() {{
            new slot<>(gridTable);
        }};
    public String title;
    public Generated<T, ?>[] generated = (Generated<T, ?>[]) new Object[0];
    public Column<T, ?>[] columns = (Column<T, ?>[]) new Object[0];
    public Component1<div, T> selectedRow;
    public Component1<div, T> expandedRow;
    public RowAdder<T> customRowAdder;
    public AsyncSupplier<T> onRowAdd;
    public AsyncHandler<T> onRowChange;
    public AsyncHandler<T> onRowDelete;
}
