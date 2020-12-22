package com.greact.generate.util;

public class CompileException extends RuntimeException {
    public enum ERROR {
        CANNOT_BE_DECLARED_AS_ASYNC,
        MUST_BE_DECLARED_AS_ASYNC;
    }
    public final ERROR error;
    public CompileException(ERROR error, String message) {
        super(message);
        this.error = error;
    }
}
