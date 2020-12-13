package greact.sample.plainjs;

import com.over64.greact.dom.HTMLNativeElements.div;
import com.over64.greact.model.components.Component;

public class _05CustomComponent implements Component<div> {
    @Override public void mount() {
        render(new div() {{
            new _05Child() {{
                mainAnswer = 42;
            }};
        }});
    }
}
