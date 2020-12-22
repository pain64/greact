package greact.sample.plainjs;

import com.over64.greact.dom.HTMLNativeElements.*;

public class _04DependsOn implements Component0<div> {

    @Override
    public div mount() {
        int[] list = new int[]{1, 2, 3};

        return new div() {{
            new ul() {{
                dependsOn = list;
                for (var x : list)
                    new li() {{
                        new a("text:" + x);
                    }};
            }};

            new button("no effects expected") {{
                onclick = () -> effect(list);
            }};
        }};
    }
}