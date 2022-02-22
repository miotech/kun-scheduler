package com.miotech.kun.commons.utils;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class CalculationUtilsTest {

    @Test
    public void getAverage_InputWithoutOutlier() {
        List<Long> sample = ImmutableList.of(1L, 3L);
        Double result = CalculationUtils.getAverageWithoutOutliers(sample);
        assertThat(result, is(2.0));
    }

    @Test
    public void getAverage_InputWithOutlier_shouldExclude() {
        List<Long> sample = ImmutableList.of(1L, 2L, 3L, 4L, 5L, 6L, 100L);
        Double result = CalculationUtils.getAverageWithoutOutliers(sample);
        assertThat(result, is(3.5));
    }

}