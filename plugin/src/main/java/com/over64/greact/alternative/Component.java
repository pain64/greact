package com.over64.greact.alternative;

import com.over64.greact.di.DI;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Component {
    default void effect(Object expression) {}
    default <U> U server(Function<DI.Data, U> onServer) { return null; }
}
