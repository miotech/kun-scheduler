package com.miotech.kun.workflow.core.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.miotech.kun.workflow.utils.DateTimeUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@JsonDeserialize
public class Tick implements Comparable<Tick>{
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final String time;

    @JsonCreator
    public Tick(@JsonProperty("time") String time) {
        this.time = time;
    }

    public Tick(OffsetDateTime dateTime) {
        this(dateTime.format(FORMATTER));
    }

    public String getTime() {
        return time;
    }

    public long toEpochSecond() {
        return LocalDateTime.parse(time, FORMATTER).toEpochSecond(DateTimeUtils.systemDefaultOffset());
    }

    public OffsetDateTime toOffsetDateTime() {
        return OffsetDateTime.of(LocalDateTime.parse(time, FORMATTER), DateTimeUtils.systemDefaultOffset());
    }

    @Override
    public String toString() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Tick tick = (Tick) o;
        return Objects.equals(time, tick.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time);
    }

    @Override
    public int compareTo(Tick tick) {
        if(this == null){
            return 1;
        }
        return this.toOffsetDateTime().compareTo(tick.toOffsetDateTime());
    }
}
