package com.over64.greact.uikit;
import java.util.Date;
import com.greact.model.JSExpression;

public class Dates {

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
}
