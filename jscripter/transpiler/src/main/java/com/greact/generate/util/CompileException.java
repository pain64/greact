package com.greact.generate.util;

public class CompileException extends RuntimeException {
    public enum ERROR {
        CANNOT_BE_DECLARED_AS_ASYNC,
        ASYNC_INVOCATION_NOT_ALLOWED,
        MEMBER_REF_USED_INCORRECT;
    }
    public final ERROR error;
    public CompileException(ERROR error, String message) {
        super(message);
        this.error = error;
    }
}
