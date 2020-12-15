package greact.sample.plainjs;

import com.over64.greact.model.components.Component;

import static com.over64.greact.dom.HTMLNativeElements.*;

public class _06SlotBareNoArgs implements Component<div> {
    boolean showHint = true;

    @Override public div mount() {
        return new div() {{
            new _06Conditional<h1>() {{
                cond = showHint; // FIXME: this must be as constructor arg
                doThen = () -> new h1("This is the hint");
                doElse = () -> new h1("The hint is hidden");
            }};
            new button("show/hide") {{
                onclick = () -> effect(showHint = !showHint);
            }};
        }};
    }
}
