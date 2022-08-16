package com.miotech.kun.monitor.sla.model;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class BacktrackingSlaConfigTest {

    @Test
    public void testGetDeadline_nullValue() {
        Integer maxLevel = 5;
        Integer minDeadline = null;
        Long rootDefinitionId = 1L;
        BacktrackingSlaConfig slaConfig = new BacktrackingSlaConfig(maxLevel, minDeadline, rootDefinitionId);
        String deadline = slaConfig.getDeadline();
        assertThat(deadline, nullValue());
    }

    @Test
    public void testGetDeadline_deadlineGtNow() {
        Clock.fixed(OffsetDateTime.now(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        OffsetDateTime oneMinuteLater = OffsetDateTime.now().plusMinutes(1);
        Integer maxLevel = 5;
        int hours = oneMinuteLater.getHour();
        int minutes = oneMinuteLater.getMinute();
        Integer minDeadline = hours * 60 + minutes;
        Long rootDefinitionId = 1L;

        BacktrackingSlaConfig slaConfig = new BacktrackingSlaConfig(maxLevel, minDeadline, rootDefinitionId);
        String deadline = slaConfig.getDeadline();
        assertThat(deadline, is(oneMinuteLater.withSecond(0).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))));
    }

    @Test
    public void testGetDeadline_deadlineIsNegative() {
        Clock.fixed(OffsetDateTime.now(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        OffsetDateTime lastMinuteOfTheDay = OffsetDateTime.now().withHour(23).withMinute(59).withSecond(0);
        Integer maxLevel = 5;
        Integer minDeadline = -1;
        Long rootDefinitionId = 1L;

        BacktrackingSlaConfig slaConfig = new BacktrackingSlaConfig(maxLevel, minDeadline, rootDefinitionId);
        String deadline = slaConfig.getDeadline();
        assertThat(deadline, is(lastMinuteOfTheDay.withSecond(0).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))));
    }

    @Test
    public void testGetDeadline_deadlineIsNegativeAndLtNow() {
        Clock.fixed(OffsetDateTime.now(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        OffsetDateTime threeMinutesLater = OffsetDateTime.now().plusMinutes(3);
        Integer maxLevel = 5;
        Integer minDeadline = -1439;
        Long rootDefinitionId = 1L;

        BacktrackingSlaConfig slaConfig = new BacktrackingSlaConfig(maxLevel, minDeadline, rootDefinitionId);
        String deadline = slaConfig.getDeadline();
        assertThat(deadline, is(threeMinutesLater.withSecond(0).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))));
    }

    @Test
    public void testGetDeadline_deadlineIsTomorrow() {
        Clock.fixed(OffsetDateTime.now(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        OffsetDateTime oneMinutesAgo = OffsetDateTime.now().minusMinutes(1);
        Integer maxLevel = 5;
        Integer minDeadline = oneMinutesAgo.getHour() * 60 + oneMinutesAgo.getMinute();
        Long rootDefinitionId = 1L;

        BacktrackingSlaConfig slaConfig = new BacktrackingSlaConfig(maxLevel, minDeadline, rootDefinitionId);
        String deadline = slaConfig.getDeadline();
        assertThat(deadline, is(oneMinutesAgo.plusDays(1).withSecond(0).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))));
    }

}
