package com.over64.greact.sample;

import com.over64.greact.GReact;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.dom.HtmlElement;
import com.over64.greact.model.components.Component;


public class DemoTest implements Component<div> {
    int nUsers = 1;
    int[] list = new int[]{1, 2, 3};

    static record Frag(Runnable lambda, Integer[] sc) {
    }

    Frag $frag0;


    @Override
    public div mount() {

        ($frag0 = new Frag(() -> {


        }, new Integer[]{null, 0})).lambda.run();

//        ($frag0 = new com$ove64$greact$dom$Frag(() => {
//
//
//        })).fn();

        new ul() {{
            dependsOn = list;
            new Child() {{
                answer = 42;
            }};
            for (var x : list)
                new li() {{
                    new a("text:" + x);
                }};
        }};

        return new div() {{
            new h1("GReact users: " + nUsers);
            new button("increment") {{
                onclick = () -> GReact.effect(nUsers += 1);
            }};
        }};
    }
}
