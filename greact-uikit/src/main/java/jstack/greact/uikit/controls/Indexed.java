package jstack.greact.uikit.controls;

public class Indexed<T> { // FIXME: make inner class of Select
    public final int i;
    public final T element;
    public final String caption;

    public Indexed(int i, T element, String caption) {
        this.i = i;
        this.element = element;
        this.caption = caption;
    }
}
