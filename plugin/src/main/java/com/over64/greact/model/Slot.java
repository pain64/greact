package com.over64.greact.model;

import org.over64.jscripter.std.js.DocumentFragment;

public class Slot<P, F> {
    public static class NoArgs {
    }

    @FunctionalInterface
    public interface SlotF0 {
        void apply(DocumentFragment dom);
    }

    @FunctionalInterface
    public interface SlotF1<T1> {
        void apply(DocumentFragment dom, T1 p1);
    }

    public final P p;
    public final F f;

    public Slot(P p, F f) {
        this.p = p;
        this.f = f;
    }
}
