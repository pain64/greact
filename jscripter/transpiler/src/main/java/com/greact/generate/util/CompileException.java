package com.greact.generate.util;

public class CompileException extends RuntimeException {
    public enum ERROR {
        CANNOT_BE_DECLARED_AS_ASYNC,
        ASYNC_INVOCATION_NOT_ALLOWED,
        MEMBER_REF_USED_INCORRECT,
        CANNOT_BE_CREATED_VIA_NEW,
        PROHIBITION_OF_INHERITANCE_FOR_JS_NATIVE_API,
        THE_METHOD_MUST_BE_DECLARED_AS_DO_NOT_TRANSPILE,
        ERASED_INTERFACE_CAN_EXTEND_ONLY_ERASED_INTERFACE,
        INSTANCE_OF_NOT_APPLICABLE_TO_ERASED_INTERFACE,
        FUNCTIONAL_INTERFACE_CAN_EXTEND_ONLY_FUNCTIONAL_INTERFACE,
        THE_METHOD_CANNOT_BE_DECLARED_DEFAULT
    }
    public final ERROR error;
    public CompileException(ERROR error, String message) {
        super(message);
        this.error = error;
    }
}
