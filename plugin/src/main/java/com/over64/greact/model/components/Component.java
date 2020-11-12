package com.over64.greact.model.components;


import org.over64.jscripter.std.js.DocumentFragment;
import org.over64.jscripter.std.js.HTMLElement;

public interface Component extends Mountable {
    void mount(HTMLElement dom);
}
