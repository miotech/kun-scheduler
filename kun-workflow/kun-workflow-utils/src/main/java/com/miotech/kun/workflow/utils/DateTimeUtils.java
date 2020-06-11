package com.miotech.kun.workflow.utils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class DateTimeUtils {
    public static OffsetDateTime fromTimestamp(Timestamp timestamp) {
        if (timestamp == null) return null;
        return OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp.getTime()),
                ZoneId.systemDefault());
    }
}
