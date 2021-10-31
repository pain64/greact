package com.over64.greact.uikit;

import com.greact.model.JSExpression;

public class Dates {
    public static String toLocaleDateString(java.sql.Date date) {
        return JSExpression.of("new Date(date).toLocaleDateString()");
    }

    public static Dates toLocaleString(String date) {
        return JSExpression.of("new Date(date).toLocaleDateString()");
    }

    public static String toLocaleString(java.util.Date date) {
        return JSExpression.of("new Date(date).toLocaleDateString()");
    }
}
