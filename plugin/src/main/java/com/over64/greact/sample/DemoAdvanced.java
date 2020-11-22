package com.over64.greact.sample;

import com.over64.greact.GReact;
import com.over64.greact.dom.HTMLNativeElements.button;
import com.over64.greact.dom.HTMLNativeElements.div;
import com.over64.greact.dom.HTMLNativeElements.h1;
import com.over64.greact.dom.HtmlElement;
import com.over64.greact.model.components.Component;

import static com.over64.greact.GReact.effect;

public class DemoAdvanced implements Component {
    /*
    class Demo {
        constructor() {
          this.mode = "M1";
          this.showUsers = true;
        }

        mount(dest) {
          var users = ["John", "Doe", "Ivan", "Igor"];

          // Greact.mount
          this._mount0$1 = (dest, frag) => {
            this.p0$1 = dest;
            this.s0$1 = dest.childElementCount + frag.childElementCount;

            if(this.showUsers) {
              for(let user of users) {
                var el4 = document.createElement('h1'); {
                  el4.innerText = "name " + user;
                }
                frag.appendChild(el4);
              }
            } else {
              var el5 = document.createElement('h1'); {
                el5.innerText = "user show disabled";
              }
              frag.appendChild(el5);
            }

            this.c0$1 = dest.childElementCount + frag.childElementCount - this.s0$1;
          }

          this._effect$showUsers = () => {
            // drop old
            var from = this.p0$1.childNodes[this.s0$1];
            for(var i = 0; i < this.c0$1; i++) {
              var next = from.nextSibling;
              this.p0$1.removeChild(from);
              from = next;
            }

            // render new
            var frag = document.createDocumentFragment();
            this._mount0$1(this.p0$1, frag);

            // insert new
            if(from) this.p0$1.insertBefore(frag, from);
            else this.p0$1.appendChild(frag);
          }

          ((dest) => {
            var frag = document.createDocumentFragment();

            switch(this.mode) {
              case "M1":
                var el1 = document.createElement('h1'); {
                  el1.innerText = 'selected M1 mode';
                }
                frag.appendChild(el1)
                break;
              case "M2":
                var el2 = document.createElement('h1'); {
                  el2.innerText = 'selected M1 mode';
                }
                frag.appendChild(el2)
                break;
            }

            var el3 = document.createElement('button'); {
              el3.innerText = 'toggle show users ' + users.length;
              el3.onclick = () => {
                this.showUsers = !this.showUsers;
                this._effect$showUsers();
              }
            }

            frag.appendChild(el3);
            this._mount0$1(dest, frag);
            dest.appendChild(frag);
          })(dest);
          // end Greact.mount
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


    @Override public void mount(HtmlElement dom) {
        RPC.server(
            () -> new String[]{"Ivan", "John", "Iborg"},
            users -> GReact.mount(dom, new div() {{
                new uikit.pagination<>(users) {{
                    by = 5;
                    item((user) -> new h1("user with name " + user));
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

                new button("toggle show users " + users.length) {{    // view 0$0
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
