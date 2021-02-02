package greact.sample.plainjs.demo;

import com.over64.greact.dom.HTMLNativeElements.*;

public class Pagination<T> implements Component1<div, T[]> {
    T[] data;
    Component1<div, T[]> page;

    @Override public div mount(T[] data) {
        return new div() {{
            new slot<>(page, data);
        }};
    }
}
