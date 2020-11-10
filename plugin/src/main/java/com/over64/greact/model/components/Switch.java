package com.over64.greact.model.components;

import com.over64.greact.GReact;
import org.over64.jscripter.std.js.DocumentFragment;

public class Switch<T> implements Component {
    public static class Case<T> {
        final T eq;
        final Slot.SlotF0 slot;

        public Case(T eq, Slot.SlotF0 slot) {
            this.eq = eq;
            this.slot = slot;
        }
    }

    final T of;
    final Case<T>[] cases;

    @SafeVarargs
    public Switch(T of, Case<T>... cases) {
        this.of = of;
        this.cases = cases;
    }

    @Override
    public void mount(DocumentFragment dom) {
        for (var _case : cases)
            if (_case.eq.equals(of))
                GReact.mount(dom, new Slot(_case.slot));
    }
}
