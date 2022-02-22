package com.miotech.kun.commons.utils;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.List;

public class CalculationUtils {

    public static final Double getAverageWithoutOutliers(List<Long> numbers) {
        DescriptiveStatistics stats = new DescriptiveStatistics(numbers.stream().mapToDouble(Long::doubleValue).toArray());
        Double mean = stats.getMean();
        Double sd = stats.getStandardDeviation();
        List<Long> normalNumbers = new ArrayList<>();
        for (Long number : numbers) {
            if (Math.abs(number - mean) < 2 * sd) {
                normalNumbers.add(number);
            }
        }
        return new DescriptiveStatistics(normalNumbers.stream().mapToDouble(Long::doubleValue).toArray()).getMean();

    }
}
