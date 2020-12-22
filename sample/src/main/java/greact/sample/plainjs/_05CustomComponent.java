package greact.sample.plainjs;

import com.over64.greact.dom.HTMLNativeElements.*;

public class _05CustomComponent implements Component0<div> {
    @Override public div mount() {
        return new div() {{
            new _05Child() {{
                mainAnswer = 42;
            }};
        }};
    }
}
