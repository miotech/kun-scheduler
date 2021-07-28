package com.miotech.kun.commons.utils;

import java.util.Map;

public class EnvironmentUtils {

    public static String getVariable(String key) {
        return System.getenv(key);
    }

    public static Map<String, String> getVariables() {
        return System.getenv();
    }
}
