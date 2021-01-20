package greact.sample.plainjs.demo.searchbox;

import com.over64.greact.dom.HTMLNativeElements;

public abstract class Input<T> extends Control<T> {
    boolean required = true;
    int maxWidth = 0;
    int maxLength = 0;
    T value;

    abstract T parseValueOpt(String src);

    @Override public HTMLNativeElements.div mount() {
        var self = this;
        return new HTMLNativeElements.div() {{
            new HTMLNativeElements.input() {{
                onchange = ev -> {
                    self.value = parseValueOpt(ev.target.value);
                    self.ready = self.value != null;
                    self.onReadyChanged.run();
                };
            }};
        }};
    }
}
