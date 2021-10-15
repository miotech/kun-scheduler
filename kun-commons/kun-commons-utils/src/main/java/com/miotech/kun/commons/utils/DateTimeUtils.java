package com.miotech.kun.commons.utils;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

public class DateTimeUtils {
    public static final DateTimeFormatter ISO_DATETIME_NANO_DATETIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
            .appendPattern("XXX")
            .toFormatter();

    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;
    private static final DateTimeFormatter MINUTE_PRECISION_DATETIME_PATTERN = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private static Clock globalClock = Clock.systemUTC();

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
        globalClock = Clock.systemUTC();
    }

    /**
     * 固定时钟在当前时刻。
     * @return
     */
    public static OffsetDateTime freeze() {
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("UTC"));
        Clock fixed = Clock.fixed(now.toInstant(), ZoneId.of("UTC"));
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
        Clock fixed = Clock.fixed(dt.toInstant(), ZoneId.of("UTC"));
        setClock(fixed);
        return dt;
    }

    public static OffsetDateTime now() {
        return OffsetDateTime.now(globalClock);
    }

    /**
     * Converts a {@link java.sql.Timestamp Timestamp} object to {@link java.time.OffsetDateTime OffsetDatetime} object
     * at nanosecond-level precision.
     * @param timestamp the {@link java.sql.Timestamp Timestamp} object to transform
     * @return converted {@link java.time.OffsetDateTime OffsetDatetime} object
     */
    public static OffsetDateTime fromTimestamp(Timestamp timestamp) {
        if (timestamp == null) return null;
        OffsetDateTime parsedOffsetDatetime = OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp.getTime()),
                ZoneId.of("UTC"));
        /**
         * Fix: OffsetDatetime object from <code>Instant.ofEpochMilli(timestamp.getTime())</code>
         * can only provide millisecond precision transformation. The following line fixes this issue and provides
         * nanoseconds precision conversion.
         */
        return parsedOffsetDatetime
                .minusNanos(parsedOffsetDatetime.getNano())
                .plusNanos(timestamp.getNanos());
    }

    /**
     * Converts and normalize an {@link java.time.OffsetDateTime OffsetDatetime} object to millisecond-level precision
     * @param time an {@link java.time.OffsetDateTime OffsetDatetime} object to convert
     * @return normalized object
     */
    public static OffsetDateTime atMillisecondPrecision(OffsetDateTime time) {
        if (time == null) {
            return null;
        }
        int nanos = time.getNano();
        return time.minusNanos(nanos % 1_000_000);
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

    public static OffsetDateTime todayMorning(){
        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(),LocalTime.MIN);
        return OffsetDateTime.of(localDateTime,ZONE_OFFSET);
    }

    /**
     * Convert a long epoch timestamp to {OffsetDateTime}
     * @param longEpochTimeVar timestamp in unix epoch 64-bit integer format
     * @return converted offset datetime
     */
    public static OffsetDateTime fromTimestamp(long longEpochTimeVar) {
        return fromTimestamp(new Timestamp(longEpochTimeVar));
    }


}
