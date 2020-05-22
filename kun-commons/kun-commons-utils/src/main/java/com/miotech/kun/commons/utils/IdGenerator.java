package com.miotech.kun.commons.utils;

import com.google.common.base.Preconditions;

/**
 * A customized ID Generator based on Snowflake
 * but replaced 10-bits `worker & datacenter id` and 12-bits `serial id`
 * with 10-bits `serial id` and 12-bits `reserved` bitwise.
 * By default, reserved digits are all assigned with 0
 * Generated ID bitmap:
 * +----------+-----------+-----------------+-----------------+-----------------+
 * |  USAGE   |  unused   |    timestamp    |     serial      |   reserved      |
 * +----------+-----------+-----------------+-----------------+-----------------+
 * | POSITION | 63(1 bit) |  22-62(41 bits) | 12-21 (10 bits) |  0-11 (12 bits) |
 * +----------+-----------+-----------------+-----------------+-----------------+
 */
public class IdGenerator {
    private static final long EPOCH = 1577232000000L;
    private static final long SERIAL_ID_BITS = 10L;
    private static final long RESERVED_BITS = 12L;
    private static final long MAX_RESERVED = ~(-1L << RESERVED_BITS);  // 4095
    private static final long MAX_SERIAL_ID = ~(-1L << SERIAL_ID_BITS);  // 1023
    private static final long SERIAL_ID_LEFT_SHIT = RESERVED_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = RESERVED_BITS + SERIAL_ID_BITS;
    private static final long RESERVED_MASK = ~(-1L << RESERVED_BITS);

    private long serial = 0L;
    private long lastTimestamp = -1L;

    private static final IdGenerator INSTANCE = new IdGenerator();

    public IdGenerator() {
        /* construct instance by default */
    }

    public synchronized long nextId() {
        long timestamp = timeGen();
        Preconditions.checkState(timestamp >= lastTimestamp, "Clock moved backwards. Refuse generating id for timestamp %d", lastTimestamp - timestamp);
        if (lastTimestamp == timestamp) {
            serial = (serial + 1) & MAX_SERIAL_ID;
            if (serial == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            serial = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT) | (serial << SERIAL_ID_LEFT_SHIT);
    }

    public synchronized long combine(long baseId, int reserved) {
        Preconditions.checkArgument(reserved > 0, "Reserved ID should be positive: %d", reserved);
        Preconditions.checkArgument(reserved <= MAX_RESERVED, "Reserved ID should not exceed max value %d, but found: %d", MAX_RESERVED, reserved);
        Preconditions.checkArgument((baseId & RESERVED_MASK) == 0, "Base ID should not contain any preset value, but found: %d", baseId & RESERVED_MASK);
        return (baseId | ((long) reserved));
    }

    private long tilNextMillis (long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    public static IdGenerator getInstance() {
        return INSTANCE;
    }
}
