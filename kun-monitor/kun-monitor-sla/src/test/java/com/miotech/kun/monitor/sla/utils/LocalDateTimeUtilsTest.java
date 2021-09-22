package com.miotech.kun.monitor.sla.utils;

import org.junit.Test;

import java.time.OffsetDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LocalDateTimeUtilsTest {

    @Test
    public void testConvert() {
        OffsetDateTime now = OffsetDateTime.now();
        LocalDateTimeUtils.freezeAt(now);

        int convert = LocalDateTimeUtils.convert();
        assertThat(convert, is(now.getHour() * 60 + now.getMinute()));
    }

}
