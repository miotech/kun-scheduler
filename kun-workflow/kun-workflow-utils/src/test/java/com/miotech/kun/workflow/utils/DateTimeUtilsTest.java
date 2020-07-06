package com.miotech.kun.workflow.utils;

import org.junit.Test;

import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DateTimeUtilsTest {
    @Test
    public void testFreezeAt() {
        // process
        DateTimeUtils.freezeAt("200001011310");
        OffsetDateTime result = DateTimeUtils.now();

        // verify
        assertThat(result.getYear(), is(2000));
        assertThat(result.getMonthValue(), is(1));
        assertThat(result.getDayOfMonth(), is(1));
        assertThat(result.getHour(), is(13));
        assertThat(result.getMinute(), is(10));
        assertThat(result.getSecond(), is(0));

        // teardown
        DateTimeUtils.resetClock();
    }
}