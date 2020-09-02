package com.miotech.kun.commons.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^\\}]+)\\}\\}");

    private StringUtils() {}

    public static String repeatJoin(String str, String delimiter, int times) {
        return String.join(delimiter, repeat(str, times));
    }

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

    public static String resolveWithVariable(String rawText, Map<String, String> variables) {
        final Matcher matcher = VARIABLE_PATTERN.matcher(rawText);

        String result = rawText;
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String key = matcher.group(i);
                String value = variables.get(key);
                if (value != null) {
                    result = result.replace(String.format("{{%s}}", key), value);
                } else {
                    throw new IllegalArgumentException("Cannot resolve variable key `" + key + "`");
                }
            }
        }
        return result;
    }

    public static String join(Collection<String> stringSlices, String separator) {
       StringBuilder stringBuilder = new StringBuilder();
        Iterator<String> iter = stringSlices.iterator();
        while (iter.hasNext()) {
            String slice = iter.next();
            stringBuilder.append(slice);
            if (iter.hasNext()) {
                stringBuilder.append(separator);
            }
        }
        return stringBuilder.toString();
    }
}
