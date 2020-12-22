package greact.sample.plainjs;

import com.over64.greact.dom.HTMLNativeElements.*;

//FIXME: this class must be inner class of _07SlotOneArg
public class _07Decorator implements Component0<div> {
    Component0<h1> forDecorate = () -> null;

    @Override public div mount() {
        return new div() {{
            style.border = "1px red solid";
            new slot<>(forDecorate);
        }};
    }
}