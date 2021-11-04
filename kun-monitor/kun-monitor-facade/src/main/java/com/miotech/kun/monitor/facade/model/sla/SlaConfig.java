package com.miotech.kun.monitor.facade.model.sla;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class SlaConfig {

    private final Integer level;

    private final Integer hours;

    private final Integer minutes;

    private final String deadlineCron;

    @JsonCreator
    public SlaConfig(@JsonProperty("level") Integer level, @JsonProperty("hours") Integer hours,
                     @JsonProperty("minutes") Integer minutes, @JsonProperty("deadlineCron") String deadlineCron) {
        this.level = level;
        this.hours = hours;
        this.minutes = minutes;
        this.deadlineCron = deadlineCron;
    }

    public Integer getLevel() {
        return level;
    }

    public Integer getHours() {
        return hours;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public String getDeadlineCron() {
        return deadlineCron;
    }

    public String getDeadline(String timeZone) {
        if(this.hours == null && this.minutes == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime deadlineDateTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), hours, minutes)
                .atZone(ZoneId.of(timeZone)).withZoneSameInstant(ZoneId.systemDefault());

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        int tv = deadlineDateTime.getHour() * 60 + deadlineDateTime.getMinute();
        int ctv = now.getHour() * 60 + now.getMinute();
        if (ctv < tv) {
            return deadlineDateTime.format(pattern);
        }

        return deadlineDateTime.plusDays(1).format(pattern);
    }

    public Integer getDeadlineValue(String timeZone) {
        if(this.hours == null && this.minutes == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime deadlineDateTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), hours, minutes)
                .atZone(ZoneId.of(timeZone)).withZoneSameInstant(ZoneId.systemDefault());
        return deadlineDateTime.getHour() * 60 + deadlineDateTime.getMinute();
    }

    public Integer getPriority() {
        return level;
    }

}
