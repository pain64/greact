package greact.sample.plainjs;

import com.over64.greact.model.components.Component;
import static com.over64.greact.dom.HTMLNativeElements.h1;

public class _05Child implements Component<h1> {
    //FIXME: this class must be inner class of _05CustomComponent
    int mainAnswer;

    @Override public void mount() {
        render(new h1("the main answer is: " + mainAnswer));
    }
}
