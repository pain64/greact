package com.over64.greact.alternative;

import com.over64.greact.dom.HTMLNativeElements.*;

class Grid<T> implements Component0<table> {
    final T[] list;
    Component1<many<td>, T> row = (__) -> null;
    Component1<div, T> selected = (__) -> null;

    T current = null;

    Grid(T[] list) {this.list = list;}

    @Override public table mount() {
        return new table() {{
            for (var item : list)
                new tr() {{
                    style.color = item == "current" ? "red" : "black";
                    onclick = () -> effect(current = item);
                    new slot<>(row, item);
                }};
            if (current != null)
                new slot<>(selected, current);
        }};
    }
}
