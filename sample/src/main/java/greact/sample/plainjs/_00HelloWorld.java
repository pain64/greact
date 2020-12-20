package greact.sample.plainjs;

import com.over64.greact.dom.HTMLNativeElements.h1;
import com.over64.greact.model.components.Component;

public class _00HelloWorld implements Component<h1> {
    @Override public h1 mount() {
        return new h1("hello, world!");
    }
}
