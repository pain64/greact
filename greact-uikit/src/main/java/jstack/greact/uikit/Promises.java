package jstack.greact.uikit;

import jstack.jscripter.transpiler.model.JSExpression;
import jstack.jscripter.transpiler.model.async;

import java.util.function.BiFunction;
import jstack.greact.dom.Runnable;

public class Promises {
    @async public static <T> T awaitPromise(BiFunction<Runnable, Runnable, T> func) {
        return JSExpression.ofAsync("await new Promise(func)");
    }
}
