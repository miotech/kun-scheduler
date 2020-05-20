package com.miotech.kun.workflow.utils;

import java.util.HashMap;
import java.util.Map;
import org.testng.util.Strings;

public class StringUtils {
    public static String escape(String s) { return escape(s, "\""); }

    public static String escape(String s, String quote) { return quote + s + quote; }

    public static boolean isNullOrEmpty(String s) { return Strings.isNullOrEmpty(s); }

    public static boolean isDuplicate(Iterable<String> s) {
        Map<String, Boolean> map = new HashMap<>();
        for (String v : s) {
            if (map.get(v) != null) {
                return true;
            }
            map.put(v, true);
        }
        return false;
    }
}
