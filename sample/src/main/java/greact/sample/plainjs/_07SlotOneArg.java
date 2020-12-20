package greact.sample.plainjs;

import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.model.components.Component;

public class _07SlotOneArg implements Component<div> {
    @Override public div mount() {
        return new div() {{
            new _07Decorator() {{
                forDecorate = () -> new h1("decorated text!");
            }};
        }};
    }
}