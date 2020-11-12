package com.over64.greact.model.components;

import org.over64.jscripter.std.js.DocumentFragment;
import org.over64.jscripter.std.js.HTMLElement;

public class Slot implements Component {

    public interface SlotF0 {
        void mount();
    }

    public interface SlotF1<T> {
        void mount(T t);
    }

    public Slot(SlotF0 slot) { }
    public <T> Slot(T t, SlotF1<T> slot) { }

    @Override
    public void mount(HTMLElement dom) { }
}
