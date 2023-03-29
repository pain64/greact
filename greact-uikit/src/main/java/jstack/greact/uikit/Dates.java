package jstack.greact.uikit;

import java.util.Date;

import jstack.jscripter.transpiler.model.JSExpression;

public class Dates {
    public static Date now() {
        return JSExpression.of("new Date().getTime()");
    }
    public static Date fromUnixTime(long millis) {
        return JSExpression.of("millis");
    }
    public static Date parse(String date) {
        return JSExpression.of("new Date(date).getTime()");
    }
    public static long getTime(Date date) {
        return JSExpression.of("date");
    }
    public static String toLocaleString(Date date) {
        return JSExpression.of("new Date(date).toLocaleString()");
    }
    public static String toLocaleDateString(Date date) {
        return JSExpression.of("new Date(date).toLocaleDateString()");
    }

    public static String toLocaleTimeString(Date date) {
        return JSExpression.of("new Date(date).toLocaleTimeString()");
    }
    public static int getYear(java.util.Date date) {
        return JSExpression.of("new Date(date).getFullYear()");
    }
    public static int getMonth(java.util.Date date) {
        return JSExpression.of("new Date(date).getMonth()");
    }
    public static int getDate(java.util.Date date) {
        return JSExpression.of("new Date(date).getDate()");
    }
    public static int getHours(java.util.Date date) {
        return JSExpression.of("new Date(date).getHours()");
    }
    public static int getMinutes(java.util.Date date) {
        return JSExpression.of("new Date(date).getMinutes()");
    }
    public static int getSeconds(java.util.Date date) {
        return JSExpression.of("new Date(date).getSeconds()");
    }
    public static int getMilliseconds(java.util.Date date) {
        return JSExpression.of("new Date(date).getMilliseconds()");
    }
}
