package com.over64.greact.sample;

import org.over64.jscripter.std.js.DocumentFragment;

import static com.over64.greact.GReact.effect;
import static com.over64.greact.GReact.render;

public class DemoAdvanced {
    enum Mode {M1, M2}

    Mode mode = Mode.M1;
    boolean showUsers = true;

    public DemoAdvanced(DocumentFragment dom) {
        var users = new String[]{"Ivan", "John", "Iborg"};

        Runnable toggle = () -> effect(showUsers = !showUsers);

        render(dom, """
            <Switch of={mode}>
              <caseIf eq={Mode.M1}>
                <h1>selected M1 mode</h1>
              </caseIf>
              <caseIf eq={Mode.M2}>
                <h1>selected M2 mode</h1>
              </caseIf>
            </Switch>
                        
            <button onclick={toggle}>
              toggle show users
            </button>
                        
            <If cond={showUsers}>
              <doThen>
                <List of={users}>
                  <item(user)><h1>{user}</h1></item>
                </List>
              </doThen>
              <doElse><h1>user show disabled</h1></doElse>
            </If>""", List.class, Switch.class, If.class);
    }
}
