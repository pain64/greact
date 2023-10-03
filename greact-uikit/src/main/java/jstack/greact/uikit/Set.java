package jstack.greact.uikit;

import jstack.jscripter.transpiler.model.JSExpression;

public class Set<T> {
    public static <T> Set<T> of(T[] elements) {
        return JSExpression.of("new Set(:1)", (Object) elements);
    }
}
