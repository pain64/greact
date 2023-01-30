package jstack.greact.uikit;

import jstack.jscripter.transpiler.model.MemberRef;
import jstack.jscripter.transpiler.model.async;

public class Generated<T, U> {
    @FunctionalInterface public interface AsyncSupplier<T> {
        @async T supply();
    }

    final String[] memberNames;
    final AsyncSupplier<U> supplier;

    public Generated(MemberRef<T, U> ref, AsyncSupplier<U> supplier) {
        this.memberNames = ref.memberNames();
        this.supplier = supplier;
    }

}
