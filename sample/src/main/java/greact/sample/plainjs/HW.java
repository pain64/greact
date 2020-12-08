package greact.sample.plainjs;

import com.over64.greact.GReact;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.dom.HtmlElement;
import com.over64.greact.model.components.Component;

import static com.over64.greact.GReact.effect;

public class HW implements Component {
    // boolean showUsers = true;
    int nUsers = 1;

    @Override
    public void mount(HtmlElement dom) {

        var users = new String[]{"Ivan", "John", "Iborg"};

        GReact.mount(dom, new div() {{
//            new button("toggle show users " + users.length) {{
//                onclick = () -> effect(showUsers = !showUsers);
//            }};
//
//            if (showUsers)
//                for (var user : users)
//                    new h1("name" + user);
//            else
//                new h1("user show disabled");

            new h1() {{ innerText = "GReact users: " + nUsers; }};

            if(nUsers > 10)
                new h1() {{ innerText = "too much users: " + nUsers; }};

            new button() {{
                innerText = "increment";
                onclick = () -> {
                    GReact.effect(nUsers += 1);
                };
            }};
        }});
    }
}