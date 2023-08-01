package jstack.jscripter.std.java.lang;

import jstack.jscripter.transpiler.model.JSNativeAPI;

@JSNativeAPI public class Error {
    private java.lang.String message;
    public Error(java.lang.String message) {
        this.message = message;
    }
    public java.lang.String getMessage() {
        return message;
    }
}

