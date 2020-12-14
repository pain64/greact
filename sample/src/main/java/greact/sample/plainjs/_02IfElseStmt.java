package greact.sample.plainjs;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.model.components.Component;

public class _02IfElseStmt implements Component<div> {
    boolean showUsers = true;

    @Override public div mount() {
        var users = new String[]{"Ivan", "John", "Iborg"};

        return new div() {{
            if (showUsers)
                for (var user : users)
                    new h1("name" + user);
            else
                new h1("user show disabled");

            new button("toggle show users " + users.length) {{
                onclick = () -> effect(showUsers = !showUsers);
            }};
        }};
    }
}