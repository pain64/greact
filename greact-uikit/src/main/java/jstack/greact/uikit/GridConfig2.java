package jstack.greact.uikit;

import jstack.greact.dom.HTMLElement;
import jstack.greact.dom.HTMLNativeElements.*;
import jstack.jscripter.transpiler.model.ErasedInterface;
import jstack.jscripter.transpiler.model.Async;

import java.util.function.Supplier;

public class GridConfig2<T> {
    @FunctionalInterface public interface AsyncHandler<T> {
        @Async void handle(T value);
    }
    @FunctionalInterface public interface AsyncSupplier<T> {
        @Async T supply(T value);
    }

    @ErasedInterface public interface RowHandler<T> {
        void updateRow(T row);
    }

    @FunctionalInterface public interface RowActionsSupplier<T> {
        Component0<div> supply(RowHandler<T> handler, T row);
    }

    public static class RowAction<T> {
        Supplier<Component0<? extends HTMLElement>> view;
        final AsyncHandler<T> handler;

        public RowAction(Supplier<Component0<? extends HTMLElement>> view, AsyncHandler<T> handler) {
            this.view = view;
            this.handler = handler;
        }

        public RowAction(String iconClassName, String title, AsyncHandler<T> handler) {
            var theTitle = title;
            this.view = () -> new div() {{
                new div() {{ className = iconClassName; this.title = theTitle; }};
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
    public RowActionsSupplier<T> rowActions = (handler, row) -> new div();
    public AsyncSupplier<T> onRowAdd;
    public AsyncHandler<T> onRowChange;
    public AsyncHandler<T> onRowDelete;
}
