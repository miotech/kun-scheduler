package com.miotech.kun.workflow.utils;

import com.cronutils.model.Cron;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class CronUtilsTest {


    @Test
    public void testSecondSchedule_should_throws_exception() throws IllegalStateException {
        String cronExp = "*/2 0 0 * * ?";
        Cron cron = CronUtils.convertStringToCron(cronExp);

        //verify
        Exception ex = assertThrows(IllegalArgumentException.class,() -> CronUtils.validateCron(cron));
        assertEquals("config seconds in cron is not supported yet",ex.getMessage());

    }

    @Test
    public void testGetNextUTCTime() {
        String cronExpression1 = "0 0 0 * * ?";
        Cron cron1 = CronUtils.convertStringToCron(cronExpression1);
        OffsetDateTime utcNow = DateTimeUtils.now();
        Optional<OffsetDateTime> next = CronUtils.getNextUTCExecutionTime(cron1, utcNow, "Asia/Shanghai");
        assertThat(next.get().getHour(), is(16));
    }

}
