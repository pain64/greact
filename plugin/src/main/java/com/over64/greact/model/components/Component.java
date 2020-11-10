package com.over64.greact.model.components;


import org.over64.jscripter.std.js.DocumentFragment;

public interface Component extends Mountable {
    void mount(DocumentFragment dom);
}
