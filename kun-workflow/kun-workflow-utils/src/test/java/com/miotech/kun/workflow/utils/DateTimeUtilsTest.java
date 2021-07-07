package com.miotech.kun.workflow.utils;

import org.junit.Test;

import java.sql.Timestamp;
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

    @Test
    public void fromTimestamp_withValidTimestampObject_shouldConvertAtNanosecondsPrecision() {
        Timestamp timestamp = new Timestamp(1625114072000l);
        timestamp.setNanos(903664124);
        OffsetDateTime convertedDatetime = DateTimeUtils.fromTimestamp(timestamp);
        assertThat(convertedDatetime.getYear(), is(2021));
        assertThat(convertedDatetime.getMonthValue(), is(7));
        assertThat(convertedDatetime.getDayOfMonth(), is(1));
        assertThat(convertedDatetime.getHour(), is(4));
        assertThat(convertedDatetime.getMinute(), is(34));
        assertThat(convertedDatetime.getSecond(), is(32));
        assertThat(convertedDatetime.getNano(), is(903664124));
    }
}
