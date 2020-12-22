package greact.sample.plainjs;

import com.over64.greact.dom.HTMLNativeElements.*;


public class _08SlotOneArg implements Component0<div> {
    @Override
    public div mount() {
        var list = new String[]{"one", "two", "three"};
        return new div() {{
            new _08ListDecorator(list) {{
                forDecorate = s -> new h2("text: " + s);
            }};
        }};
    }
}
