package jstack.jscripter.transpiler.model;

public class JSExpression {
    public static native <T> T of(String s, Object... args);
    @Async public static native <T> T ofAsync(String s, Object... args);
}
