package com.over64.greact.model.components;

import com.over64.greact.GReact;
import org.over64.jscripter.std.js.DocumentFragment;

import static org.over64.jscripter.std.js.Globals.document;

public class Seq<T> implements Component {
    final T[] of;
    public Slot.SlotF1<T> item;

    public Seq(T[] of) {
        this.of = of;
    }

    @Override
    public void mount(DocumentFragment dom) {
        var itemDom = document.createDocumentFragment();
        for (var itemValue : of) {
            GReact.mount(itemDom, new Slot(itemValue, item));
            dom.appendChild(itemDom);
        }
    }
}
