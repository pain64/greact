package com.over64.greact.sample;

import com.over64.greact.model.Slot;
import com.over64.greact.model.Slot.NoArgs;
import com.over64.greact.model.Slot.SlotF1;
import org.over64.jscripter.std.js.DocumentFragment;

import static org.over64.jscripter.std.js.Globals.document;

public class List<T> {
    public List(DocumentFragment dom, T[] of, Slot<NoArgs, SlotF1<T>> item) {
        var itemDom = document.createDocumentFragment();
        for (var itemValue : of) {
            item.f.apply(itemDom, itemValue);
            dom.appendChild(itemDom);
        }
    }
}
