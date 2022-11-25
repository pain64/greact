package com.over64.greact.uikit.controls;

import com.over64.greact.dom.HTMLNativeElements.*;

public abstract class Control<T> implements Component0<div> {
    public Runnable onReadyChanged = () -> {};
    public boolean ready = false;
    public boolean optional = false;
    public T value;
    protected String label;
    public int slots = 1;
    public abstract Control child();
}
