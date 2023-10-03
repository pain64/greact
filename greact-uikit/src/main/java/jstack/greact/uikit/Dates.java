package jstack.greact.uikit;

import java.util.Date;

import jstack.jscripter.transpiler.model.JSExpression;

public class Dates {
    public static Date now() {
        return JSExpression.of("new Date().getTime()");
    }
    public static Date fromUnixTime(long millis) {
        return JSExpression.of(":1", millis);
    }
    public static Date parse(String date) {
        return JSExpression.of("new Date(:1).getTime()", date);
    }
    public static long getTime(Date date) {
        return JSExpression.of(":1", date);
    }
    public static String toLocaleString(Date date) {
        return JSExpression.of("new Date(:1).toLocaleString()", date);
    }
    public static String toLocaleDateString(Date date) {
        return JSExpression.of("new Date(:1).toLocaleDateString()", date);
    }

    public static String toLocaleTimeString(Date date) {
        return JSExpression.of("new Date(:1).toLocaleTimeString()", date);
    }
    public static int getYear(java.util.Date date) {
        return JSExpression.of("new Date(:1).getFullYear()", date);
    }
    public static int getMonth(java.util.Date date) {
        return JSExpression.of("new Date(:1).getMonth()", date);
    }
    public static int getDate(java.util.Date date) {
        return JSExpression.of("new Date(:1).getDate()", date);
    }
    public static int getHours(java.util.Date date) {
        return JSExpression.of("new Date(:1).getHours()", date);
    }
    public static int getMinutes(java.util.Date date) {
        return JSExpression.of("new Date(:1).getMinutes()", date);
    }
    public static int getSeconds(java.util.Date date) {
        return JSExpression.of("new Date(:1).getSeconds()", date);
    }
    public static int getMilliseconds(java.util.Date date) {
        return JSExpression.of("new Date(:1).getMilliseconds()", date);
    }
}
