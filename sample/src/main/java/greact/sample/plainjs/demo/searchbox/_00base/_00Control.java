package greact.sample.plainjs.demo.searchbox._00base;

import com.over64.greact.dom.HTMLNativeElements.*;

public abstract class _00Control<T> implements Component0<div> {
    public Runnable onReadyChanged = () -> {};
    public boolean ready = false;
    public T value;
    protected String _label;
    public abstract _00Control child();
}
