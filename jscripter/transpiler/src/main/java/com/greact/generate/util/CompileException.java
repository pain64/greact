package com.greact.generate.util;

public class CompileException extends RuntimeException {
    public enum ERROR {
        CANNOT_BE_DECLARED_AS_ASYNC,
        ASYNC_INVOCATION_NOT_ALLOWED,
        MEMBER_REF_USED_INCORRECT,
        CANNOT_BE_CREATED_VIA_NEW,
        PROHIBITION_OF_INHERITANCE_FOR_JS_NATIVE_API
    }
    public final ERROR error;
    public CompileException(ERROR error, String message) {
        super(message);
        this.error = error;
    }
}
