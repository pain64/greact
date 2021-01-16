package greact.sample.plainjs.demo.searchbox;

import com.over64.greact.dom.HTMLNativeElements.*;

abstract class Control<T> implements Component0<div> {
    Runnable onReadyChanged = () -> {};
    boolean ready = false;
    T value;
}
