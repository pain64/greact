package jstack.greact.uikit;

public class Row<T> {
    boolean selected = false;
    boolean expanded = false;
    boolean editing = false;
    boolean freshRow = false;
    T data;

    public Row(T data) {this.data = data;}
}
