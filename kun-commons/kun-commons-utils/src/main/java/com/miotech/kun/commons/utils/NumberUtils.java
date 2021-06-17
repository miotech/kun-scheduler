package com.miotech.kun.commons.utils;

public class NumberUtils {
    private NumberUtils() {}

    public static Double toDouble(Long value) {
        if (value != null) {
            return Double.valueOf(value);
        }
        return null;
    }
}
