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

        int hrs = 0;
        if (this.hours != null) {
            hrs = this.hours;
        }

        int min = 0;
        if (this.minutes != null) {
            min = this.minutes;
        }

        int tv = hrs * 60 + min;
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        ZonedDateTime deadlineDateTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), hrs, min)
                .atZone(ZoneId.of(timeZone)).withZoneSameInstant(ZoneId.systemDefault());
        int ctv = now.getHour() * 60 + now.getMinute();
        if (ctv < tv) {
            return deadlineDateTime.format(pattern);
        }

        return deadlineDateTime.plusDays(1).format(pattern);
    }

    public Integer getPriority() {
        return level;
    }

}
