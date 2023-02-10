package jstack.greact.uikit;

import jstack.greact.dom.HTMLElement;
import jstack.greact.dom.HTMLNativeElements.*;
import jstack.jscripter.transpiler.model.async;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GridConfig2<T> {
    @FunctionalInterface public interface AsyncHandler<T> {
        @async void handle(T value);
    }
    @FunctionalInterface public interface AsyncSupplier<T> {
        @async T supply(T value);
    }

    public static class RowAction<T> {
        Supplier<Component0<? extends HTMLElement>> view;
        final AsyncHandler<T> handler;

        public RowAction(Supplier<Component0<? extends HTMLElement>> view, AsyncHandler<T> handler) {
            this.view = view;
            this.handler = handler;
        }

        public RowAction(Consumer<div> iconContent, AsyncHandler<T> handler) {
            this.view = () -> new div() {{
                new div() {{ iconContent.accept(this); }};
            }};
            this.handler = handler;
        }
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
    public RowAdder<T> customRowAdder; // FIXME: rowAdder ???
    public RowAction<T>[] rowActions = (RowAction<T>[]) new Object[0];
    public AsyncSupplier<T> onRowAdd;
    public AsyncHandler<T> onRowChange;
    public AsyncHandler<T> onRowDelete;
}
