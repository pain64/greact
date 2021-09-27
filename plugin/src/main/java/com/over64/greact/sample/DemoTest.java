package com.over64.greact.sample;

import com.over64.greact.dom.GReact;
import com.over64.greact.dom.HTMLNativeElements;
import com.over64.greact.dom.HTMLNativeElements.Component0;
import com.over64.greact.dom.HTMLNativeElements.div;
import com.over64.greact.dom.HTMLNativeElements.h1;

public class DemoTest implements Component0<div> {

    static class Mapped implements Component0<div> {
        @Override public div mount() {
            final div _root = (div) GReact.element;
            // some code
            return GReact.entry(() ->
                GReact.<h1>make(_root, "h1", _el2 ->
                    _el2.innerText = "hello, world!"));

//          let _root = GReact.element;
//          // some code
//          return GReact.bind(_root, _el1 => {
//              GReact.make(_el1, "h1", _el2 => {
//                  _el2.innerText = "hello, world!"
//              });
//          });
        }
    }

    @Override public div mount() {
        var list = new String[]{"one", "two", "three"};
        return new div() {{
            new h1() {{
                innerText = "hello, world!";
            }};
        }};
    }
}
