package com.over64.greact.sample;

import com.over64.greact.GReact;
import com.over64.greact.dom.HTMLNativeElements.*;
import com.over64.greact.model.components.Component;

import static com.over64.greact.GReact.effect;

public class DemoAdvanced implements Component<li> {
    /*
    class greact$sample$plainjs$HW extends Object {
  constructor() {
    super();
    this.list = [1, 2, 3]
    this.$frag0 = null
  }

  mount(dom) {
    {
      let $el0 = com$over64$greact$dom$Globals.document.createElement('div');
      {
        (this.$frag0 = new com$over64$greact$dom$Fragment(() => {
          this.$frag0.cleanup();
          {
            let $el1 = com$over64$greact$dom$Globals.document.createElement('ul');
            {
              // $el1.dependsOn = this.list;
              for(let x in this.list) {
                let $el2 = com$over64$greact$dom$Globals.document.createElement('li');
                {
                  {
                    let $el3 = com$over64$greact$dom$Globals.document.createElement('a');
                    $el3.innerText = 'text:' + x;
                    $el2.appendChild($el3);
                  }
                }
                $el1.appendChild($el2);
              }
            }
            this.$frag0.appendChild($el1);
          }
        }, $el0)).render();
        {
          let $el4 = com$over64$greact$dom$Globals.document.createElement('button');
          $el4.innerText = 'do effect';
          {
            $el4.onclick = () =>this.effect$list(this.list);
          }
          $el0.appendChild($el4);
        }
      }
      dom.appendChild($el0);
    }
  }

  effect$list(arg0) {
    this.$frag0.render();
  }
}


    new Demo().mount(document.getElementById('view'))

<html>
<body>
  <div id="view"></div>
</body>
</html>

     */
    enum Mode {M1, M2}

    Mode mode = Mode.M1;
    boolean showUsers = true;


    @Override
    public void mount(li element) {
        RPC.server(
            () -> new String[]{"Ivan", "John", "Iborg"},
            users -> GReact.mount(element, new div() {{
                new uikit.pagination<>(users) {{
                    by = 5;
                    item = user -> new h1("user with name " + user);
                }};

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
                    onclick = () -> effect(showUsers = !showUsers);   // view 0$0
                }};                                                   // view 0$0

                if (showUsers)                                        // view 0$1
                    for (var user : users)                            // view 0$1
                        new h1("name" + user);                        // view 0$1
                else                                                  // view 0$1
                    new h1("user show disabled");                     // view 0$1
            }}));
    }
}
