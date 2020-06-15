package com.miotech.kun.workflow.utils;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.google.common.base.Preconditions;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

public class CronUtils {

    private CronUtils() {}

    /* By default we use Quartz cron expression */
    private static final CronType DEFAULT_CRON_TYPE = CronType.QUARTZ;

    /**
     * Convert a quartz cron expression string to cron object, this cron object will never be null
     * @param cronExpression quartz cron expression in string
     * @throws java.lang.IllegalArgumentException if expression does not match cron definition
     * @return Cron instance
     */
    public static Cron convertStringToCron(String cronExpression) {
        return convertStringToCron(cronExpression, DEFAULT_CRON_TYPE);
    }

    /**
     * Convert cron expression string to cron object, this cron object will never be null
     * @param cronExpression cron expression in string
     * @param cronType selected cron definition type: CRON4J, QUARTZ, UNIX, SPRING
     * @throws java.lang.IllegalArgumentException if expression does not match cron definition
     * @return
     */
    public static Cron convertStringToCron(String cronExpression, CronType cronType) {
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(cronType);
        CronParser parser = new CronParser(cronDefinition);
        return parser.parse(cronExpression);
    }

    /**
     * Validate a cron expression in String. Throws IllegalArgumentException if the cron expression is invalid
     * @param cron cron object to be validate
     * @throws IllegalArgumentException if the cron expression is invalid
     */
    public static void validateCron(Cron cron) {
        cron.validate();
    }

    /**
     * Get a OffsetDateTime object (optional) that represents the time for next execution from current time by given cron expression
     * @param cron cron object
     * @return an optional ZonedDateTime that represents the time for next execution
     */
    public static Optional<OffsetDateTime> getNextExecutionTimeFromNow(Cron cron) {
        return getNextExecutionTime(cron, DateTimeUtils.now());
    }

    /**
     * Get a OffsetDateTime object (optional) that represents the time for next execution by given cron expression and timebase
     * @param cron cron object
     * @param timebase datetime pivot
     * @return an optional ZonedDateTime that represents the time for next execution
     */
    public static Optional<OffsetDateTime> getNextExecutionTime(Cron cron, OffsetDateTime timebase) {
        Preconditions.checkNotNull(timebase, "Invalid argument `cron`: null");
        Preconditions.checkNotNull(timebase, "Invalid argument `timebase`: null");

        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        Optional<ZonedDateTime> zonedDateTimeOptional = executionTime.nextExecution(timebase.toZonedDateTime());
        return zonedDateTimeOptional.map(ZonedDateTime::toOffsetDateTime);
    }

    /**
     * Get a OffsetDateTime object (optional) that represents the time for last execution from current time by given cron expression
     * @param cron cron object
     * @return an optional ZonedDateTime that represents the time for last execution
     */
    public static Optional<OffsetDateTime> getLastExecutionTimeFromNow(Cron cron) {
        return getLastExecutionTime(cron, DateTimeUtils.now());
    }

    /**
     * Get a OffsetDateTime object (optional) that represents the time for last execution by given cron expression and timebase
     * @param cron cron object
     * @param timebase datetime pivot
     * @return an optional ZonedDateTime that represents the time for last execution
     */
    public static Optional<OffsetDateTime> getLastExecutionTime(Cron cron, OffsetDateTime timebase) {
        Preconditions.checkNotNull(timebase, "Invalid argument `cron`: null");
        Preconditions.checkNotNull(timebase, "Invalid argument `timebase`: null");

        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        Optional<ZonedDateTime> zonedDateTimeOptional = executionTime.lastExecution(timebase.toZonedDateTime());
        return zonedDateTimeOptional.map(ZonedDateTime::toOffsetDateTime);
    }
}
