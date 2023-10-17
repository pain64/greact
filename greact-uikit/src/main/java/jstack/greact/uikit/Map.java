package jstack.greact.uikit;

import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.Static;

import java.util.function.Function;

public class Map<K, V> {
    static void bar() {

    }
    static void foo() {
        bar(); // а тут this.bar()
    }

    void baz() {
        bar(); // тут нужно this.constructor.bar()
    }
    public static <K, V> Map<K, V> make() { return JSExpression.of("new Map()"); }
    public native void set(K key, V value);
    @Static public V get(K key) {
        V value = JSExpression.of("this.get(:1)", key);
        value = JSExpression.of("typeof :1 === 'undefined' ? null : :1", value);
        return value;
    }
    public native V[] values();
    public native K[] keys();
    /* sooooka */
    public native Object[][] entries();


    @Static public V computeIfAbsent(K key, Function<K, V> mapping) {
        // FIXME: некорректный вызов @Static метода на this
        //var value = this.get(key);
        V value = JSExpression.of("this.get(:1)", key);
        if (JSExpression.of("typeof :1 !== 'undefined'", value)) return value;

        var computed = mapping.apply(key);
        this.set(key, computed);
        return computed;
    }

    @Static public int size() {
        return JSExpression.of("this.size");
    }
}
