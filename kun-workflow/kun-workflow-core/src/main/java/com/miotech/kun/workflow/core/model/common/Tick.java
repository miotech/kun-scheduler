package com.miotech.kun.workflow.core.model.common;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class Tick {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final String time;

    public Tick(String time) {
        this.time = time;
    }

    public Tick(OffsetDateTime dateTime) {
        this(dateTime.format(FORMATTER));
    }

    @Override
    public String toString() {
        return time;
    }
}
