package jstack.greact.uikit;

import jstack.jscripter.transpiler.model.JSExpression;
import jstack.greact.uikit.controls.Cascade;

import java.util.function.Function;
import java.util.function.Predicate;

public class Array<T> {
    @SafeVarargs public static <T> T[] of(T... args) {
        return JSExpression.of("Array.from(:1)", (Object) args);
    }

    public static <T> T find(T[] self, Predicate<T> predicate) {
        var found = JSExpression.of(":1.find(:2)", self, predicate);
        return JSExpression.of("typeof :1 === 'undefined' ? null : :1", found);
    }

    public static <T> boolean exists(T[] self, Predicate<T> predicate) {
        return JSExpression.of("typeof :1.find(:2) !== 'undefined'", self, predicate);
    }

    public static <T> long indexOf(T[] self, T el) {
        return JSExpression.of(":1.indexOf(:2)", self, el);
    }

    public static <T> T contains(T[] self, T el) {
        return JSExpression.of(":1.indexOf(:2) !== -1", self, el);
    }

    public static <T> void push(T[] array, T value) {
        JSExpression.of(":1.push(:2)", array, value);
    }
    public static <T> void unshift(T[] array, T value) {
        JSExpression.of(":1.unshift(:2)", array, value);
    }

    public static <A, B> B[] map(A[] from, Function<A, B> mapper) {
        return JSExpression.of(":1.map(:2)", from, mapper);
    }

    public static <A> A[] filter(A[] from, Function<A, Boolean> predicate) {
        return JSExpression.of(":1.filter(:2)", from, predicate);
    }

    public static <A, B> Pair<A, B>[] zip(A[] left, B[] right) {
        Pair<A, B>[] result = (Pair<A, B>[]) new Object[0];
        for (var i = 0; i < left.length; i++)
            Array.push(result, new Pair<>(left[i], right[i]));
        return result;
    }

    public static <T> T[] spliced(T[] array, long start, long deleteCount) {
        var cloned = JSExpression.<T[]>of("Array.from(:1)", (Object) array);
        JSExpression.of(":1.splice(:2, :3)", cloned, start, deleteCount);
        return cloned;
    }

    public static <T> T last(T[] array) {
        return array.length == 0 ? null : array[array.length - 1];
    }

    public static <T> T[] unique(T[] src) {
        return JSExpression.of("Array.from(new Set(:1))", (Object) src);
    }

    public static <T> Array<T> from(Set<T> set) {
        return JSExpression.of("Array.from(:1)", set);
    }
}
