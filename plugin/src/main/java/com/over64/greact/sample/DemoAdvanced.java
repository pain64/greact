package com.over64.greact.sample;

import com.over64.greact.GReact;
import com.over64.greact.model.components.Component;
import com.over64.greact.model.components.HTMLNativeElements.button;
import com.over64.greact.model.components.HTMLNativeElements.div;
import com.over64.greact.model.components.HTMLNativeElements.h1;
import com.over64.greact.model.components.If;
import com.over64.greact.model.components.Seq;
import com.over64.greact.model.components.Switch;
import com.over64.greact.model.components.Switch.Case;
import org.over64.jscripter.std.js.DocumentFragment;

import static com.over64.greact.GReact.effect;

public class DemoAdvanced implements Component {
    /*

    class Demo {
    constructor() {
      console.log("new Demo")
      this.mode = "M1";
      this.users = ["John", "Doe", "Ivan", "Igor"];
      this.showUsers = true;

      this.c1 = null;
      this.e1 = null;
    }

    mount(dest) {
      console.log("call mount")
      this._mount(dest, 0);
    }

	_mount(dest, effect) { // dest is div
    var before = null;

    switch(effect) {
      case 1:
        for(var i = 0; i < this.c1; i++) {
          before = this.e1.previousSibling;
          dest.removeChild(this.e1);
          this.e1 = before;
        }
    }

	  var frag = document.createDocumentFragment();

	  if(effect == 0) {
      switch(this.mode) {
        case "M1":
          console.log("render M1");
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
        el3.innerText = 'toggle show users ' + this.users.length;
        el3.onclick = () => {
          this.showUsers = !this.showUsers;
          this._mount(dest, 1);
        }
      }
      frag.appendChild(el3);
    }

	  if(effect == 0 || effect == 1) {
      var s1 = frag.childElementCount

      if(this.showUsers) {
        for(let user of this.users) {
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

      this.c1 = frag.childElementCount - s1
      this.e1 = frag.lastChild
    }


    if(before != null)
      dest.insertBefore(frag, before.nextSibling);
    else
      dest.appendChild(frag);

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
    public void mount(DocumentFragment dom) {
        var users = new String[]{"Ivan", "John", "Iborg"};

        GReact.mount(dom, new div() {{
            new Switch<>(mode,
                new Case<>(Mode.M1, () -> new h1("selected M1 mode")),
                new Case<>(Mode.M2, () -> new h1("selected M2 mode")));

            new button("toggle show users " + users.length) {{
                onclick = () -> effect(showUsers = !showUsers);
            }};

            new If(showUsers) {{
                doThen = () ->
                    new Seq<>(users) {{
                        item = user -> new h1("name" + user);
                    }};
                doElse = () -> new h1("user show disabled");
            }};
        }});

        GReact.mount(dom, new div() {{
            switch (mode) {
                case M1 -> new h1("selected M1 mode");
                case M2 -> new h1("selected M2 mode");
            }

            new button("toggle show users " + users.length) {{
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
