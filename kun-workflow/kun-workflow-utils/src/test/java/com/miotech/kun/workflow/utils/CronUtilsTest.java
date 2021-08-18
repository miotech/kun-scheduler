package com.miotech.kun.workflow.utils;

import com.cronutils.model.Cron;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;



public class CronUtilsTest {


    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testSecondSchedule_should_throws_exception() throws IllegalStateException {
        String cronExp = "*/2 0 0 * * ?";
        Cron cron = CronUtils.convertStringToCron(cronExp);

        //verify
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("config seconds in cron is not supported yet");

        CronUtils.validateCron(cron);

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
