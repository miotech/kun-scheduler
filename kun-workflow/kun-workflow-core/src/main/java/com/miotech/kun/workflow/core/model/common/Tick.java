package com.miotech.kun.workflow.core.model.common;

import com.miotech.kun.workflow.utils.DateTimeUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Tick {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final ZoneOffset ZONE_OFFSET = DateTimeUtils.now().getOffset();

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tick tick = (Tick) o;
        return Objects.equals(time, tick.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time);
    }
}
