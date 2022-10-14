package com.over64.greact.uikit;

import com.greact.model.ErasedInterface;
import com.greact.model.MemberRef;
import com.greact.model.async;

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
