package greact.sample.plainjs;

import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.model.components.Component;

public class _00HelloWorld implements Component<h1> {
    @Override
    public void mount() {
        render(new h1("hello, world!"));
    }
}
