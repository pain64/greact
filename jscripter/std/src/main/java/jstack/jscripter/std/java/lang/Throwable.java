package jstack.jscripter.std.java.lang;

public class Throwable extends Error {
    Throwable cause = null;

    public Throwable() { super(); }

    public Throwable(java.lang.String message) {
        super(message);
    }

    public Throwable(java.lang.String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }

    public Throwable(Throwable cause) {
        super();
        this.cause = cause;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
