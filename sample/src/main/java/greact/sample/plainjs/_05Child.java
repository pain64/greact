package greact.sample.plainjs;

import static com.over64.greact.dom.HTMLNativeElements.*;

public class _05Child implements Component0<h1> {
    //FIXME: this class must be inner class of _05CustomComponent
    int mainAnswer;

    @Override public h1 mount() {
        return new h1("the main answer is: " + mainAnswer);
    }
}
