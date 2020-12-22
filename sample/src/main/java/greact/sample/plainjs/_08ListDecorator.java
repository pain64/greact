package greact.sample.plainjs;

import com.over64.greact.dom.HTMLNativeElements;
import com.over64.greact.dom.HTMLNativeElements.*;

public class _08ListDecorator implements Component0<div> {
    final String[] list;
    Component1<h2, String> forDecorate = s -> null;

    _08ListDecorator(String[] list) {
        this.list = list;
    }

    @Override public div mount() {
        return new div() {{
            style.border = "1px red solid";
            for (var s : list)
                new div() {{
                    style.margin = "4px";
                    style.border = "1px green solid";
                    new slot<>(forDecorate, s);
                }};
        }};
    }
}