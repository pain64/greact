package greact.sample.plainjs;

import com.over64.greact.GReact;
import com.over64.greact.dom.HTMLNativeElements.button;
import com.over64.greact.dom.HTMLNativeElements.div;
import com.over64.greact.dom.HTMLNativeElements.h1;
import com.over64.greact.dom.HtmlElement;
import com.over64.greact.model.components.Component;

import static com.over64.greact.GReact.effect;

public class HW implements Component {
    boolean showUsers = true;

    @Override
    public void mount(HtmlElement dom) {

        var users = new String[]{"Ivan", "John", "Iborg"};

        GReact.mount(dom, new div() {{
            new button() {{
                innerText = "toggle show users " + users.length;
                onclick = () -> effect(showUsers = !showUsers);
            }};

            if (showUsers)
                for (var user : users)
                    new h1("name" + user);
            else
                new h1("user show disabled");
        }});
    }
}