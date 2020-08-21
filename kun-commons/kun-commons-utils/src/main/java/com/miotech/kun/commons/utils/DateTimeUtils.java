package com.miotech.kun.commons.utils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class DateTimeUtils {
    public static final DateTimeFormatter ISO_DATETIME_NANO_DATETIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
            .appendPattern("XXX")
            .toFormatter();

    public static OffsetDateTime fromTimestamp(Timestamp timestamp) {
        if (timestamp == null) return null;
        return OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp.getTime()),
                ZoneId.systemDefault());
    }
}
