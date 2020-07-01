package com.miotech.kun.metadata.databuilder.extract.tool;

import org.apache.commons.lang3.StringUtils;

public class TableOrFieldNameEscapeUtil {

    private TableOrFieldNameEscapeUtil() {
    }

    public static String escape(String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }

        return "\"" + name + "\"";
    }

}
