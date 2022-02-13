package com.over64.greact.uikit;

import com.greact.model.JSExpression;
import com.greact.model.Static;

import java.util.function.Function;

public class Map<K, V> {
    public static <K, V> Map<K, V> make() {return JSExpression.of("new Map()");}
    public native void set(K key, V value);
    @Static public  V get(K key) {
        V value = JSExpression.of("this.get(key)");
        value = JSExpression.of("typeof value === 'undefined' ? null : value");
        return value;
    }
    public native V[] values();
    public native K[] keys();
    /* sooooka */
    public native Object[][] entries();


    @Static public V computeIfAbsent(K key, Function<K, V> mapping) {
         // FIXME: некорректный вызов @Static метода на this
         //var value = this.get(key);
         V value = JSExpression.of("this.get(key)");
         if(JSExpression.of("typeof value !== 'undefined'")) return value;

         var computed = mapping.apply(key);
         this.set(key, computed);
         return computed;
    }

    @Static public int size() {
        return JSExpression.of("this.size");
    }
}
