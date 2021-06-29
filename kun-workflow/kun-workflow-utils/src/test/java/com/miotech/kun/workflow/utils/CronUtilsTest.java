package com.miotech.kun.workflow.utils;

import com.cronutils.model.Cron;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CronUtilsTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testSecondSchedule_should_throws_exception() throws IllegalStateException{
        String cronExp = "*/2 0 0 * * ?";
        Cron cron = CronUtils.convertStringToCron(cronExp);

        //verify
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("kun not support second-scheduler currently");

        CronUtils.validateCron(cron);

    }
}
