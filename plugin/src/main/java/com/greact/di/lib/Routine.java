package com.greact.di.lib;

@FunctionalInterface
public interface Routine<E extends Throwable> {
    void call() throws E;
}