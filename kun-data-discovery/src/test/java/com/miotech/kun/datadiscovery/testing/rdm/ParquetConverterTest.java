package com.miotech.kun.datadiscovery.testing.rdm;

import com.miotech.kun.datadiscovery.util.DateFormatFactory;
import org.apache.parquet.io.api.Binary;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static com.miotech.kun.datadiscovery.service.rdm.file.ParquetConverter.*;
import static org.junit.Assert.assertEquals;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-08-15 22:46
 **/

public class ParquetConverterTest {


    @Test
    void testTimestamp() {
        Timestamp timestamp = Timestamp.valueOf("2022-08-17 16:44:08.375");
        byte[] bytes = convertTimeStamp(timestamp);
        String timestampString = getTimestampString(Binary.fromReusedByteArray(bytes));
        Timestamp timestampTest = Timestamp.valueOf(timestampString);
        assertThat(timestamp.toLocalDateTime().getYear(), is(timestampTest.toLocalDateTime().getYear()));
        assertThat(timestamp.toLocalDateTime().getMonth(), is(timestampTest.toLocalDateTime().getMonth()));
        assertThat(timestamp.toLocalDateTime().getDayOfYear(), is(timestampTest.toLocalDateTime().getDayOfYear()));
        assertThat(timestamp.toLocalDateTime().getDayOfMonth(), is(timestampTest.toLocalDateTime().getDayOfMonth()));
        assertThat(timestamp.toLocalDateTime().getDayOfWeek(), is(timestampTest.toLocalDateTime().getDayOfWeek()));
        assertThat(timestamp.toLocalDateTime().getHour(), is(timestampTest.toLocalDateTime().getHour()));
        assertThat(timestamp.toLocalDateTime().getHour(), is(timestampTest.toLocalDateTime().getHour()));
        assertThat(timestamp.toLocalDateTime().getHour(), is(timestampTest.toLocalDateTime().getHour()));
        assertThat(timestamp.toLocalDateTime().getMinute(), is(timestampTest.toLocalDateTime().getMinute()));
        assertThat(timestamp.toLocalDateTime().getSecond(), is(timestampTest.toLocalDateTime().getSecond()));
        assertThat(timestamp.toLocalDateTime().getNano(), is(timestampTest.toLocalDateTime().getNano()));

    }

    @Test
    void testDate() {
        String dateString = "2022-09-01";
        Integer integer = convertDate(dateString);
        String dateString1 = getDateString(integer);
        assertThat(dateString, is(dateString1));
    }


    @Test
    public void testBinaryToDecimal() throws Exception {
        String d0 = "9223372036854775807";
        assertThat(getBigDecimalString(convertBigDecimal(d0)), is(d0));
        String d1 = "-9223372036854775808";
        assertThat(getBigDecimalString(convertBigDecimal(d1)), is(d1));
        String d2 = "2147483647";
        assertThat(getBigDecimalString(convertBigDecimal(d2)), is(d2));
        String d3 = "-2147483648";
        assertThat(getBigDecimalString(convertBigDecimal(d3)), is(d3));
        String d4 = "12345678912345678";
        assertThat(getBigDecimalString(convertBigDecimal(d4)), is(d4));
        String d5 = "123456789123456.78";
        assertThat(getBigDecimalString(convertBigDecimal(d5)), is(d5));
        String d6 = "0.12345678912345678";
        assertThat(getBigDecimalString(convertBigDecimal(d6)), is(d6));
        String d7 = "-0.000102";
        assertThat(getBigDecimalString(convertBigDecimal(d7)), is(d7));
    }


}
