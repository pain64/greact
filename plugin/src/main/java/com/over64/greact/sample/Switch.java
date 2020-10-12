package com.over64.greact.sample;

import com.over64.greact.model.Slot;
import com.over64.greact.model.Slot.SlotF0;
import org.over64.jscripter.std.js.DocumentFragment;

import static org.over64.jscripter.std.js.Globals.document;

public class Switch<T> {
    public static class CaseArgs<T> {
        final T eq;

        public CaseArgs(T eq) {
            this.eq = eq;
        }
    }

    @SafeVarargs
    public Switch(DocumentFragment dom, T of, Slot<CaseArgs<T>, SlotF0>... caseIf) {
        var childDom = document.createDocumentFragment();
        for (var slot : caseIf)
            if (slot.p.eq.equals(of))
                slot.f.apply(childDom);

        dom.appendChild(childDom);
    }
}
