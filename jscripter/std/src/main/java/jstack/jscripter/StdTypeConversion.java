package jstack.jscripter;

import jstack.jscripter.std.java.lang.Exception;
import jstack.jscripter.std.java.lang.Integer;
import jstack.jscripter.std.java.lang.String;
import jstack.jscripter.std.java.lang.Throwable;
import jstack.jscripter.std.java.lang.RuntimeException;

public class StdTypeConversion {
    public static native Integer conv(java.lang.Integer x);
    public static native String conv(java.lang.String x);
    public static native RuntimeException conv(java.lang.RuntimeException x);
    public static native Exception conv(java.lang.Exception x);
    public static native Throwable conv(java.lang.Throwable x);
}
