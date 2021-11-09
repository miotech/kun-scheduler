package com.miotech.kun.monitor.sla.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class BacktrackingSlaConfig {

    private final Integer maxLevel;

    private final Integer minDeadline;

    private final Long rootDefinitionId;

    public BacktrackingSlaConfig(Integer maxLevel, Integer minDeadline, Long rootDefinitionId) {
        this.maxLevel = maxLevel;
        this.minDeadline = minDeadline;
        this.rootDefinitionId = rootDefinitionId;
    }

    public Integer getMaxLevel() {
        return maxLevel;
    }

    public Integer getMinDeadline() {
        return minDeadline;
    }

    public Long getRootDefinitionId() {
        return rootDefinitionId;
    }

    public String getDeadline() {
        if (minDeadline == null) {
            return null;
        }

        int hrs = minDeadline / 60;
        int min = minDeadline % 60;
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime deadlineDateTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), hrs, min)
                .atZone(ZoneId.systemDefault());

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        int ctv = now.getHour() * 60 + now.getMinute();
        if (ctv < minDeadline) {
            return deadlineDateTime.format(pattern);
        }

        return deadlineDateTime.plusDays(1).format(pattern);
    }
}
