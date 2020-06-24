package com.miotech.kun.datadiscover.common.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
public class DateUtil {

    public static String dateTimeMillisToString(Long dateTime) {
        return Instant.ofEpochMilli(dateTime).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static Long dateTimeToMillis(String dateTime) {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static Long dateTimeToMillis(LocalDateTime dateTime) {
        if (dateTime != null) {
            return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return null;
    }

    public static Long dateTimeToMillis(OffsetDateTime dateTime) {
        if (dateTime != null) {
            return dateTime.toInstant().toEpochMilli();
        }
        return null;
    }

    public static OffsetDateTime millisToOffsetDateTime(long millis) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }

    public static LocalDateTime millisToLocalDateTime(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC);
    }
}
