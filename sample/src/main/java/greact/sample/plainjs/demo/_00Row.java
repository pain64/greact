package greact.sample.plainjs.demo;

public class _00Row<T> {
    boolean selected = false;
    boolean expanded = false;
    boolean editing = false;
    boolean freshRow = false;
    final T data;

    public _00Row(T data) {this.data = data;}
}
