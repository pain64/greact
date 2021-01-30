package greact.sample.plainjs.demo;

import com.over64.greact.dom.HTMLNativeElements.*;

public class Pagination<T> implements Component0<div> {
    T[] data;
    Component1<div, T[]> page;

    public Pagination(T[] data) {
        this.data = data;
    }

    @Override public div mount() {
        return new div() {{
            new slot<>(page, data);
        }};
    }
}
