package com.miotech.kun.monitor.sla.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeUtils {

    private static Clock globalClock = Clock.systemDefaultZone();
    private static DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private LocalDateTimeUtils() {
    }

    public static void setClock(Clock newClock) {
        globalClock = newClock;
    }

    public static String format() {
        return LocalDateTime.now(globalClock).format(pattern);
    }

    public static int convert() {
        LocalTime localTime = LocalTime.now(globalClock);
        return convert(localTime.getHour(), localTime.getMinute());
    }

    public static int convert(int hrs, int min) {
        return hrs * 60 + min;
    }

    /**
     * 固定时钟在某一用户指定的时刻
     * @param offsetDateTime
     * @return
     */
    public static OffsetDateTime freezeAt(OffsetDateTime offsetDateTime) {
        Clock fixed = Clock.fixed(offsetDateTime.toInstant(), ZoneId.systemDefault());
        setClock(fixed);
        return offsetDateTime;
    }

}
