package com.over64.greact.uikit;

import com.greact.model.JSExpression;

public class Set<T> {
    public static <T> Set<T> of(T[] elements) {
        return JSExpression.of("new Set(elements)");
    }
}
