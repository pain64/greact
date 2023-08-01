package std.java.lang;

public class RuntimeException extends Exception {
    public RuntimeException() {
    }

    public RuntimeException(java.lang.String message) {
        super(message);
    }

    public RuntimeException(java.lang.String message, Throwable cause) {
        super(message, cause);
    }

    public RuntimeException(Throwable cause) {
        super(cause);
    }
}
