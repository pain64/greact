package greact.sample.plainjs;

import com.over64.greact.GReact;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.dom.HtmlElement;
import com.over64.greact.model.components.Component;

public class _02IfElseStmt implements Component {
    boolean showUsers = true;

    @Override
    public void mount(HtmlElement dom) {
        var users = new String[]{"Ivan", "John", "Iborg"};

        GReact.mount(dom, new div() {{
            if (showUsers)
                for (var user : users)
                    new h1("name" + user);
            else
                new h1("user show disabled");

            new button("toggle show users " + users.length) {{
                onclick = () -> GReact.effect(showUsers = !showUsers);
            }};
        }});
    }
}