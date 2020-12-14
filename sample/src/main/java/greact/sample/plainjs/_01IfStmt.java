package greact.sample.plainjs;

import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.model.components.Component;

public class _01IfStmt implements Component<div> {
    int nUsers = 1;

    @Override
    public div mount() {
        return new div() {{
            new h1("number of GReact users: " + nUsers);
            if (nUsers > 10)
                new h1("Awesome! Too much users!");

            new button("increment") {{
                onclick = () -> effect(nUsers += 1);
            }};
        }};
    }
}