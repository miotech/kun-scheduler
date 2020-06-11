package com.miotech.kun.metadata.extract.tool;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {

    public static String convertUpperCase(String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }

        boolean hasUpperCase = false;
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch)) {
                hasUpperCase = true;
            }
        }

        if (hasUpperCase) {
            return "\"" + name + "\"";
        }

        return name;
    }

}
