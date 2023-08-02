package jstack.jscripter.std.java.lang;

import jstack.jscripter.transpiler.model.JSExpression;

public class Throwable extends Error {
    Throwable cause = null;

    public Throwable() {
        super("");
    }

    public Throwable(java.lang.String message) {
        super(message);
    }

    public Throwable(java.lang.String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }

    public Throwable(Throwable cause) {
        super("");
        this.cause = cause;
    }

    public Throwable getCause() {
        return this.cause;
    }

    public java.lang.String getMessage() {
        return JSExpression.of("this.message");
    }
}
