package com.miotech.kun.commons.utils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class IdGeneratorTest {
    private static final long RESERVED_BITS = 12L;
    private static final long RESERVED_MASK = ~(-1L << RESERVED_BITS);

    @Test
    public void testCorrectness() {
        IdGenerator generator = IdGenerator.getInstance();
        long sampleId0 = generator.nextId();
        long sampleId1 = generator.nextId();
        long sampleId2 = generator.nextId();
        long sampleId3 = generator.nextId();

        /* generated IDs should always growing */
        assertThat(sampleId1 > sampleId0, is(true));
        assertThat(sampleId2 > sampleId1, is(true));
        assertThat(sampleId3 > sampleId2, is(true));

        /* reserved bits, by default, should always be 0s */
        assertThat((RESERVED_MASK & sampleId0), is(0L));
        assertThat((RESERVED_MASK & sampleId1), is(0L));
        assertThat((RESERVED_MASK & sampleId2), is(0L));
        assertThat((RESERVED_MASK & sampleId3), is(0L));
    }

    @Test
    public void testCombine() {
        IdGenerator generator = IdGenerator.getInstance();
        long baseId = generator.nextId();

        /* A random reserved value */
        int reservedId = 0x0101;

        long combinedId = IdGenerator.getInstance().combine(baseId, reservedId);

        /* combined id should contains base & reserved parts separately */
        assertThat((combinedId & RESERVED_MASK), is((long) reservedId));
        assertThat((combinedId & ~RESERVED_MASK), is(baseId));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCombine_whenExceptionThrows() {
        /* `IdGenerator.combine` should raise exceptions
           when baseId hybrids non-zero reserved part */
        IdGenerator generator = IdGenerator.getInstance();
        long baseId = generator.nextId();
        int reservedId = 0x0101;

        long combinedId = IdGenerator.getInstance().combine(baseId, reservedId);

        IdGenerator.getInstance().combine(combinedId, reservedId);
    }
}
