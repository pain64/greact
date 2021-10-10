package com.over64.greact.uikit;

import com.greact.model.JSExpression;
import com.over64.greact.uikit.controls.Cascade;

import java.util.function.Function;
import java.util.function.Predicate;

public class Array<T> {
    @SafeVarargs public static <T> T[] of(T... args) {
        return JSExpression.of("Array.from(arguments)");
    }

    public static <T> T find(T[] self, Predicate<T> predicate) {
        return JSExpression.of("self.find(predicate)");
    }

    public static <T> void push(T[] array, T value) {
        JSExpression.of("array.push(value)");
    }

    public static <A, B> B[] map(A[] from, Function<A, B> mapper) {
        return JSExpression.of("from.map(mapper)");
    }

    public static <A> A[] filter(A[] from, Cascade.Func1<A, Boolean> predicate) {
        return JSExpression.of("from.filter(predicate)");
    }

    public static <A, B> Pair<A, B>[] zip(A[] left, B[] right) {
        Pair<A, B>[] result = (Pair<A, B>[]) new Object[0];
        for (var i = 0; i < left.length; i++)
            push(result, new Pair<>(left[i], right[i]));
        return result;
    }
}
