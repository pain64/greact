package greact.sample.plainjs;

import com.over64.greact.GReact;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.dom.HtmlElement;
import com.over64.greact.model.components.Component;

public class _04DependsOn implements Component {

    @Override
    public void mount(HtmlElement dom) {
        int[] list = new int[]{1, 2, 3};

        GReact.mount(dom, new div() {{
            new ul() {{
                dependsOn = list;
                for (var x : list)
                    new li() {{
                        new a("text:" + x);
                    }};
            }};

            new button("do effect") {{
                onclick = () -> GReact.effect(list);
            }};
        }});
    }
}