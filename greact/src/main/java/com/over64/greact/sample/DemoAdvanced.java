package com.over64.greact.sample;

import com.over64.greact.dom.HTMLNativeElements;
import com.over64.greact.dom.HTMLNativeElements.button;
import com.over64.greact.dom.HTMLNativeElements.div;
import com.over64.greact.dom.HTMLNativeElements.h1;

import java.util.Optional;

public class DemoAdvanced implements HTMLNativeElements.Component0<div> {

    enum Mode {M1, M2}

    Mode mode = Mode.M1;
    boolean showUsers = true;


    @Override
    public div mount() {
        var userOption = Optional.of("John");
        var users = new String[]{"Ivan", "John", "Iborg"};
        return new div() {{
            if (userOption.isPresent())
                new h1(userOption.get());


            new div() {{
                className = "my-super-div";
                style.color = "#eee";
            }};
            // view 0
            switch (mode) {                                       // view 0$0
                case M1 -> new h1("selected M1 mode");            // view 0$0
                case M2 -> new h1("selected M2 mode");            // view 0$0
            }                                                     // view 0$0

            new button() {{    // view 0$0
                innerText = "toggle show users " + users.length;
                onclick = ev -> effect(showUsers = !showUsers);   // view 0$0
            }};                                                   // view 0$0

            if (showUsers)                                        // view 0$1
                for (var user : users)                            // view 0$1
                    new h1("name" + user);                        // view 0$1
            else                                                  // view 0$1
                new h1("user show disabled");                     // view 0$1
        }};
    }
}
