package com.miotech.kun.datadiscovery.service.rdm.file;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.miotech.kun.datadiscovery.model.enums.ColumnType;
import com.miotech.kun.datadiscovery.util.DateFormatFactory;
import jodd.time.JulianDate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hive.common.type.HiveDecimal;
import org.apache.parquet.io.api.Binary;
import org.apache.spark.sql.types.DecimalType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-08-15 22:46
 **/
@Slf4j
public class ParquetConverter {

    private static final int JULIAN_EPOCH_OFFSET_DAYS = 2440588;
    private static final long MILLIS_IN_DAY = TimeUnit.DAYS.toMillis(1);
    private static final long NANOS_PER_MILLISECOND = TimeUnit.MILLISECONDS.toNanos(1);
    private static final ThreadLocal<TimeZone> LOCAL_TIMEZONE;

    static {
        LOCAL_TIMEZONE = ThreadLocal.withInitial(() -> Calendar.getInstance().getTimeZone());
    }

    public static byte[] convertTimeStamp(Timestamp timestamp) {
        DateTime dateTime = new DateTime(timestamp, DateTimeZone.UTC);
        byte[] bytes = new byte[12];
        LocalDateTime localDateTime = LocalDateTime.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), dateTime.getSecondOfMinute());
        //转纳秒
        long nanos = dateTime.getHourOfDay() * TimeUnit.HOURS.toNanos(1) + dateTime.getMinuteOfHour() * TimeUnit.MINUTES.toNanos(1) + dateTime.getSecondOfMinute() * TimeUnit.SECONDS.toNanos(1)
                + dateTime.millisOfSecond().get() * TimeUnit.MILLISECONDS.toNanos(1);
        //转儒略日
        ZonedDateTime localT = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        ZonedDateTime utcT = localT.withZoneSameInstant(ZoneId.of("UTC"));
        JulianDate julianDate = JulianDate.of(utcT.toLocalDateTime());
        //写入INT96时间戳
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN).putLong(nanos)//8位
                .putInt(julianDate.getJulianDayNumber());//4位
        return bytes;
    }

    public static String getTimestampString(Binary timestampBinary) {
        if (timestampBinary.length() != 12) {
            return null;
        }
        byte[] bytes = timestampBinary.getBytes();
        long timeOfDayNanos = Longs.fromBytes(bytes[7], bytes[6], bytes[5], bytes[4], bytes[3], bytes[2], bytes[1], bytes[0]);
        int julianDay = Ints.fromBytes(bytes[11], bytes[10], bytes[9], bytes[8]);
        return new Timestamp(julianDayToMillis(julianDay) + (timeOfDayNanos / NANOS_PER_MILLISECOND)).toString();
    }

    private static long julianDayToMillis(int julianDay) {
        return (julianDay - JULIAN_EPOCH_OFFSET_DAYS) * MILLIS_IN_DAY;
    }


    public static String getDateString(Integer dateInt) {
        return DateFormatFactory.getFormat().format(TimeUnit.DAYS.toMillis(dateInt));
    }

    @SneakyThrows
    public static Integer convertDate(String value) {
//        from hive-exec
        long time = DateFormatFactory.getFormat().parse(value).getTime();
        long millisUtc = time + (long) LOCAL_TIMEZONE.get().getOffset(time);
        int days;
        if (millisUtc >= 0L) {
            days = (int) (millisUtc / MILLIS_IN_DAY);
        } else {
            days = (int) ((millisUtc - 86399999L) / MILLIS_IN_DAY);
        }
        return days;
    }

    public static String getBigDecimalString(Binary value) {
        DecimalType decimalType = (DecimalType) ColumnType.DECIMAL.getSparkDataType();
        int precision = decimalType.precision();
        int scale = decimalType.scale();
//        from parquet mr
        if (precision <= 18) {
            ByteBuffer buffer = value.toByteBuffer();
            byte[] bytes = buffer.array();
            int start = buffer.arrayOffset() + buffer.position();
            int end = buffer.arrayOffset() + buffer.limit();
            long unscaled = 0L;
            int i = start;
            while (i < end) {
                unscaled = (unscaled << 8 | bytes[i] & 0xff);
                i++;
            }
            int bits = 8 * (end - start);
            long unscaledNew = (unscaled << (64 - bits)) >> (64 - bits);
            if (unscaledNew <= -Math.pow(10, 18) || unscaledNew >= Math.pow(10, 18)) {
                return new BigDecimal(unscaledNew).stripTrailingZeros().toPlainString();
            } else {
                return BigDecimal.valueOf(unscaledNew / Math.pow(10, scale)).stripTrailingZeros().toPlainString();
            }
        } else {
            return new BigDecimal(new BigInteger(value.getBytes()), scale).stripTrailingZeros().toPlainString();
        }

    }

    public static Binary convertBigDecimal(String decimal) {
        DecimalType sparkDataType = (DecimalType) ColumnType.DECIMAL.getSparkDataType();
        if (decimal.length() >= sparkDataType.precision()) {
            throw new IllegalArgumentException();
        }
        BigDecimal bigDecimal = new BigDecimal(decimal, new MathContext(38 - 1)).setScale(18);
        byte[] bytes = HiveDecimal.create(bigDecimal).bigIntegerBytesScaled(sparkDataType.scale());
        ByteBuffer allocate = ByteBuffer.allocate(16);
        byte fillByte = (byte) 0;
        if (bigDecimal.compareTo(new BigDecimal(0)) < 0) {
            fillByte = (byte) 0xff;
        }
        if (bytes.length < 16) {
            byte[] bytes1 = new byte[16 - bytes.length];
            Arrays.fill(bytes1, fillByte);
            allocate.put(bytes1, 0, bytes1.length);
            allocate.put(bytes, 0, bytes.length);
            bytes = allocate.array();
        } else if (bytes.length > 16) {
            bytes = Arrays.copyOfRange(bytes, bytes.length - 16, bytes.length);
        }
        return Binary.fromConstantByteArray(bytes);
    }

}
