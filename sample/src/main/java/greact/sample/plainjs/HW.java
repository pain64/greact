package greact.sample.plainjs;

import com.over64.greact.GReact;
import com.over64.greact.dom.HTMLNativeElements.div;
import com.over64.greact.dom.HTMLNativeElements.h1;
import com.over64.greact.dom.HtmlElement;
import com.over64.greact.model.components.Component;

public class HW implements Component {

    @Override
    public void mount(HtmlElement dom) {
        GReact.mount(dom, new div() {{
            new h1() {{
                innerText = "Hello, Kitty";
            }};
        }});
    }
}