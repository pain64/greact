package greact.sample.plainjs;

import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.model.components.Component;

public class _04DependsOn implements Component<div> {

    @Override
    public void mount() {
        int[] list = new int[]{1, 2, 3};

        render(new div() {{
            new ul() {{
                dependsOn = list;
                for (var x : list)
                    new li() {{
                        new a("text:" + x);
                    }};
            }};

            new button("do effect") {{
                onclick = () -> effect(list);
            }};
        }});
    }
}