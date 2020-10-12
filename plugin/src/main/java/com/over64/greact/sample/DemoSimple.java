package com.over64.greact.sample;

import org.over64.jscripter.std.js.DocumentFragment;

import static com.over64.greact.GReact.render;


public class DemoSimple {
    /*
    class H1 {
	constructor(dom, text) {
		let me = document.createElement('h1')
		me.innerText = text
		dom.appendChild(me)

		setTimeout(() => me.innerText = 'oops', 3000)
	}
}

class Demo {
	constructor(dom) {
	    let $child1 = document.createDocumentFragment()
		new H1($child1, 'hello, world')
		dom.appendChild($child1)
	}
}

var users = Array.of("Ivan", "John");

render("""
  <h1>Hello, GReact</h1>
  <if cond={isEnabled}>
    <then>

    </then>
    <else>

    </else>
  </if>

  <switch cond={mode}>
    <case eq={MODE1}>
      <h1>enter to MODE 1</h1>
    </case>
    <case eq={MODE2}>
      <h1>enter to MODE 2</h1>
    </case>
    <case eq={MODE3}>
      <h1>enter to MODE 3</h1>
    </case>
  </switch>

  <list(user) of={users}>
    <item>
    </item>
  </list>""", Table.class)
     */

    public DemoSimple(DocumentFragment dom) {
        render(dom, """
            <H1 text="hello, GReact" />""", H1.class);
    }
}
