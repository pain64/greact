package jstack.greact.uikit.controls;

import jstack.greact.dom.HTMLNativeElements;

public abstract class Control<T> implements HTMLNativeElements.Component0<HTMLNativeElements.div> {
    public Runnable onReadyChanged = () -> {};
    public boolean ready = false;
    public boolean optional = false;
    public T value;
    protected String label;
    public int slots = 1;
    public abstract Control child();
}
