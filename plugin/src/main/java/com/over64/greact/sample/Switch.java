package com.over64.greact.sample;

import com.over64.greact.model.Slot.SlotF0;
import org.over64.jscripter.std.js.DocumentFragment;

import static org.over64.jscripter.std.js.Globals.document;

public class Switch<T> {
    public static class CaseArgs<T> {
        final T eq;
        final SlotF0 fn;

        public CaseArgs(T eq, SlotF0 fn) {
            this.eq = eq;
            this.fn = fn;
        }
    }

    @SafeVarargs
    public Switch(DocumentFragment dom, T of, CaseArgs<T>... caseIf) {
        var childDom = document.createDocumentFragment();
        for (var slot : caseIf)
            if (slot.eq.equals(of))
                slot.fn.apply(childDom);

        dom.appendChild(childDom);
    }
}
