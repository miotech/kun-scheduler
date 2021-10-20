package com.miotech.kun.commons.utils;

public class ExprUtils {

    public static String evalExecuteTimeExpr(String expr, String tick) {
        return expr.replace("${execute_time}", tick);
    }
}
