package com.over64.greact.sample;

import com.over64.greact.model.Slot;
import com.over64.greact.model.Slot.NoArgs;
import com.over64.greact.model.Slot.SlotF0;
import org.over64.jscripter.std.js.DocumentFragment;

import static org.over64.jscripter.std.js.Globals.document;

public class If {
    public If(DocumentFragment dom, boolean cond,
              Slot<NoArgs, SlotF0> doThen,
              Slot<NoArgs, SlotF0> doElse) {

        var childDom = document.createDocumentFragment();
        if (cond) doThen.f.apply(childDom);
        else doElse.f.apply(childDom);

        dom.appendChild(childDom);
    }
}
