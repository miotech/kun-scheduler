package com.miotech.kun.workflow.core.model.common;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Tick {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();

    private final String time;

    public Tick(String time) {
        this.time = time;
    }

    public Tick(OffsetDateTime dateTime) {
        this(dateTime.format(FORMATTER));
    }

    public long toEpochSecond() {
        return LocalDateTime.parse(time, FORMATTER).toEpochSecond(ZONE_OFFSET);
    }

    @Override
    public String toString() {
        return time;
    }
}
