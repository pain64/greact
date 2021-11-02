package com.over64.greact.uikit;
import java.util.Date;
import com.greact.model.JSExpression;

public class Dates {
    public static String toLocaleDateString(java.sql.Date date) {
        return JSExpression.of("new Date(date).toLocaleDateString()");
    }

    public static Date parse(String date) {
        return JSExpression.of("new Date(date).toLocaleDateString()");
    }

    public static String toLocaleString(java.util.Date date) {
        return JSExpression.of("new Date(date).toLocaleDateString()");
    }
}
