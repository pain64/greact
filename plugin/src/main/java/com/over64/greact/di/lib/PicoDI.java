package com.over64.greact.di.lib;

import java.util.Optional;

public class PicoDI {
    @FunctionalInterface
    public interface Routine<E extends Throwable> {
        void call() throws E;
    }

    public static <T, E extends Throwable> void bound(ThreadLocal<T> inst, T data, Routine<E> with) throws E {
        try {
            inst.set(data);
            with.call();
        } finally {
            inst.remove();
        }
    }

    public static <T> T di(ThreadLocal<T> inst) {
        return Optional.ofNullable(inst.get())
            .orElseThrow(() -> new IllegalStateException("thread-local di context not bound"));
    }
}
