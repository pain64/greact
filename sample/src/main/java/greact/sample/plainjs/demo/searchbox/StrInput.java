package greact.sample.plainjs.demo.searchbox;

import com.over64.greact.dom.HTMLNativeElements.*;

public class StrInput extends Control<String> {
    final boolean required;
    int maxWidth = 0;
    int maxLength = 0;
    // validation

    String value;

    public StrInput(boolean required) {
        this.required = required;
    }

    @Override public div mount() {
        var self = this;
        return new div() {{
            new input() {{
                onchange = ev -> {
                    self.value = ev.target.value;
                    self.ready = true;
                    self.onReadyChanged.run();
                };
            }};
        }};
    }
}
