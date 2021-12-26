package com.over64.greact.uikit;

import com.greact.model.JSExpression;
import com.greact.model.Static;

import java.util.function.Function;

public class Map<K, V> {
    public static <K, V> Map<K, V> make() {return JSExpression.of("new Map()");}
    public native void set(K key, V value);
    public native V get(K key);

    @Static public V computeIfAbsent(K key, Function<K, V> mapping) {
         var value = this.get(key);
         if(value != null) return value;

         var computed = mapping.apply(key);
         this.set(key, computed);
         return computed;
    }
}
