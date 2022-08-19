package com.miotech.kun.datadiscovery.util;

import java.text.SimpleDateFormat;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-08-16 10:54
 **/
public class DateFormatFactory {
    private static final String format = "yyyy-MM-dd";

    public static SimpleDateFormat getFormat() {
        return new SimpleDateFormat(format);
    }
}
