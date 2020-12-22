package greact.sample.plainjs;

import com.over64.greact.dom.HTMLNativeElements.*;

public class _07SlotOneArg implements Component0<div> {
    @Override public div mount() {
        return new div() {{
            new _07Decorator() {{
                forDecorate = () -> new h1("decorated text!");
            }};
        }};
    }
}