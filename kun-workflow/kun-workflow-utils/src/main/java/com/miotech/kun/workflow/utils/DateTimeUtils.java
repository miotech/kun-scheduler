package com.miotech.kun.workflow.utils;

import java.sql.Timestamp;
import java.time.*;

public class DateTimeUtils {
    private static Clock globalClock = Clock.systemDefaultZone();

    public static Clock getClock() {
        return globalClock;
    }

    public static void setClock(Clock newClock) {
        globalClock = newClock;
    }

    public static void resetClock() {
        globalClock = Clock.systemDefaultZone();
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
}
