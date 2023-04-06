package jstack.greact.uikit;

import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.async;

public class Promise<T> {
    @FunctionalInterface public interface AsyncFunction<A1> {
        @async void run(A1 a1);
    }
    @FunctionalInterface public interface AsyncBiFunction<A1, A2> {
        @async void load(A1 a1, A2 a2);
    }

    private final AsyncBiFunction<AsyncFunction<T>, AsyncFunction<T>> func;

    public Promise(AsyncBiFunction<AsyncFunction<T>, AsyncFunction<T>> func) {
        this.func = func;
    }
    public void then(AsyncFunction<T> function1) {
        JSExpression.of("new Promise(this.func).then(function1)");
    }
}
