package com.greact.generate.util;

public class CompileException extends RuntimeException {
    public enum ERROR {
        CANNOT_BE_DECLARED_AS_ASYNC,
        ASYNC_INVOCATION_NOT_ALLOWED,
        MEMBER_REF_USED_INCORRECT,
        CANNOT_BE_CREATED_VIA_NEW,
        PROHIBITION_OF_INHERITANCE_FOR_JS_NATIVE_API,
        THE_METHOD_MUST_BE_DECLARED_AS_DO_NOT_TRANSPILE,
        ERASED_INTERFACE_CAN_BE_INHERITED_ONLY_FROM_ERASED_INTERFACE,
        ERASED_INTERFACE_NOT_USE_OPERATOR_INSTANCE_OF
    }
    public final ERROR error;
    public CompileException(ERROR error, String message) {
        super(message);
        this.error = error;
    }
}
