package com.miotech.kun.workflow.utils;

public class StringUtils {

    public static String[] repeat(String str, int times) {
        if (str == null) return null;
        if (times <= 0) return new String[]{};

        String[] result = new String[times];
        for(int i = 0; i < times; i++) {
            result[i] = str;
        }
        return result;
    }

    public static String capitalize(String str) {
        if (str == null) return null;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String toNullableString(Object obj) {
        return obj == null ? null : obj.toString();
    }
}
