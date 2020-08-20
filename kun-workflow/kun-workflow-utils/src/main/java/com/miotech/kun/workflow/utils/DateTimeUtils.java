package com.miotech.kun.workflow.utils;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeUtils {
    private static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();
    private static final DateTimeFormatter MINUTE_PRECISION_DATETIME_PATTERN = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private static Clock globalClock = Clock.systemDefaultZone();

    private DateTimeUtils() {}

    public static ZoneOffset systemDefaultOffset() {
        return ZONE_OFFSET;
    }

    public static Clock getClock() {
        return globalClock;
    }

    public static void setClock(Clock newClock) {
        globalClock = newClock;
    }

    public static void resetClock() {
        globalClock = Clock.systemDefaultZone();
    }

    /**
     * 固定时钟在当前时刻。
     * @return
     */
    public static OffsetDateTime freeze() {
        OffsetDateTime now = OffsetDateTime.now();
        Clock fixed = Clock.fixed(now.toInstant(), ZoneId.systemDefault());
        setClock(fixed);
        return now;
    }

    /**
     * 固定时钟在某一用户指定的时刻。支持的格式为"yyyyMMdd HH:mm:ss"。
     * @param dateTimeString
     * @return
     */
    public static OffsetDateTime freezeAt(String dateTimeString) {
        OffsetDateTime dt = LocalDateTime.parse(dateTimeString, MINUTE_PRECISION_DATETIME_PATTERN).atOffset(ZONE_OFFSET);
        Clock fixed = Clock.fixed(dt.toInstant(), ZoneId.systemDefault());
        setClock(fixed);
        return dt;
    }

    public static OffsetDateTime now() {
        return OffsetDateTime.now(globalClock);
    }

    public static OffsetDateTime fromTimestamp(Timestamp timestamp) {
        if (timestamp == null) return null;
        return OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp.getTime()),
                ZoneId.systemDefault());
    }

    /**
     * Obtains an instance of {@code OffsetDateTime} from a text string which follows ISO 8601 format,
     * such as {@code 2007-12-03T10:15:30+01:00}.
     * @param isoDateTimeString ISO Datetime string
     * @throws DateTimeParseException if the text cannot be parsed
     * @return parsed OffsetDateTime. Returns null if parameter isoDateTimeString is null.
     */
    public static OffsetDateTime fromISODateTimeString(String isoDateTimeString) {
        if (isoDateTimeString == null) {
            return null;
        }
        return OffsetDateTime.parse(isoDateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
