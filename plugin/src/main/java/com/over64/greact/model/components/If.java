package com.over64.greact.model.components;

import com.over64.greact.GReact;
import org.over64.jscripter.std.js.DocumentFragment;

public class If implements Component {
    final boolean cond;
    public Slot.SlotF0 doThen = () -> {};
    public Slot.SlotF0 doElse = () -> {};

    public If(boolean cond) {
        this.cond = cond;
    }

    @Override
    public void mount(DocumentFragment dom) {
        GReact.mount(dom, new Slot(cond ? doThen : doElse));
    }
}
