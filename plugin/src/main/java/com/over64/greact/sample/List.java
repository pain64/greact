package com.over64.greact.sample;

import com.over64.greact.model.Slot.SlotF1;
import org.over64.jscripter.std.js.DocumentFragment;

import static org.over64.jscripter.std.js.Globals.document;

public class List<T> {
    public static class ItemArgs<T> {
        final SlotF1<T> fn;
        public ItemArgs(SlotF1<T> fn) {
            this.fn = fn;
        }
    }
    public List(DocumentFragment dom, T[] of, ItemArgs<T> item) {
        var itemDom = document.createDocumentFragment();
        for (var itemValue : of) {
            item.fn.apply(itemDom, itemValue);
            dom.appendChild(itemDom);
        }
    }
}
