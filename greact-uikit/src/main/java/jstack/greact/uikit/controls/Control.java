package jstack.greact.uikit.controls;

import jstack.greact.dom.HTMLNativeElements.Component0;
import jstack.greact.dom.HTMLNativeElements.div;

public abstract class Control<T> implements Component0<div> {
    public Runnable onReadyChanged = () -> {};
    public boolean ready = false;
    public boolean optional = false;
    public T value;
    protected String label;
    public int slots = 1;
    public abstract Control child();
}
