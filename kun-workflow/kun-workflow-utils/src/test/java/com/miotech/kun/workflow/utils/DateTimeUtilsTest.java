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
        Timestamp timestamp = Timestamp.valueOf("2020-09-02 21:41:17.903664124");
        OffsetDateTime convertedDatetime = DateTimeUtils.fromTimestamp(timestamp);
        assertThat(convertedDatetime.getYear(), is(2020));
        assertThat(convertedDatetime.getMonthValue(), is(9));
        assertThat(convertedDatetime.getDayOfMonth(), is(2));
        assertThat(convertedDatetime.getHour(), is(21));
        assertThat(convertedDatetime.getMinute(), is(41));
        assertThat(convertedDatetime.getSecond(), is(17));
        assertThat(convertedDatetime.getNano(), is(903664124));
    }
}
