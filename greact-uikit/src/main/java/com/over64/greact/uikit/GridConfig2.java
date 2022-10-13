package com.over64.greact.uikit;

import com.greact.model.ErasedInterface;
import com.greact.model.async;
import com.over64.greact.dom.HTMLNativeElements;
import com.over64.greact.dom.HTMLNativeElements.*;

import java.util.function.BiFunction;

public class GridConfig2<T> {
    @ErasedInterface
    @FunctionalInterface public interface AsyncHandler<T> {
        @async void handle(T value);
    }
    @ErasedInterface
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
