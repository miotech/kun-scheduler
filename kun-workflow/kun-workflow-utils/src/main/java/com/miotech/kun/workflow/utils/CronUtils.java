package com.miotech.kun.workflow.utils;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.google.common.base.Preconditions;

import java.time.*;
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
        CronField cronField = cron.retrieve(CronFieldName.SECOND);
        FieldExpression fieldExpression = cronField.getExpression();
        if(!fieldExpression.asString().equals("0")){
            throw new IllegalArgumentException("config seconds in cron is not supported yet");
        }
    }

    /**
     * Get a OffsetDateTime object (optional) that represents the time for next execution from current time by given cron expression
     * @param cron cron object
     * @return an optional ZonedDateTime that represents the time for next execution
     */
    public static Optional<OffsetDateTime> getNextExecutionTimeFromNow(Cron cron) {
        return getNextExecutionTime(cron, DateTimeUtils.now());
    }

    public static Optional<OffsetDateTime> getNextExecutionTimeByCronExpr(String cronExpr,OffsetDateTime startTime){
        return getNextExecutionTime(convertStringToCron(cronExpr),startTime);
    }

    /**
     * Get a OffsetDateTime object (optional) that represents the time for next execution by given cron expression and timebase
     * @param cron cron object
     * @param timebase datetime pivot
     * @return an optional ZonedDateTime that represents the time for next execution
     */
    public static Optional<OffsetDateTime> getNextExecutionTime(Cron cron, OffsetDateTime timebase) {
        return getNextUTCExecutionTime(cron,timebase, "UTC");
    }

    /**
     * Get a OffsetDateTime object (optional) that represents the time for next execution by given cron expression,timebase and timeZone
     * @param cronExpr cron object
     * @param timebase datetime pivot
     * @param zoneId timezone id
     * @return an optional ZonedDateTime that represents the time for next execution
     */
    public static Optional<OffsetDateTime> getNextUTCExecutionTimeByExpr(String cronExpr,OffsetDateTime timebase, String zoneId) {
        return getNextUTCExecutionTime(convertStringToCron(cronExpr),timebase,zoneId);
    }

    /**
     * Get a OffsetDateTime object (optional) that represents the time for next execution by given cron ,timebase and timeZone
     * @param cron cron object
     * @param timebase datetime pivot
     * @param zoneId
     * @return an optional ZonedDateTime that represents the time for next execution
     */
    public static Optional<OffsetDateTime> getNextUTCExecutionTime(Cron cron, OffsetDateTime timebase, String  zoneId) {
        Preconditions.checkNotNull(timebase, "Invalid argument `cron`: null");
        Preconditions.checkNotNull(timebase, "Invalid argument `timebase`: null");

        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime zonedDateTime = timebase.atZoneSameInstant(ZoneId.of(zoneId));
        Optional<ZonedDateTime> zonedDateTimeOptional = executionTime.nextExecution(zonedDateTime);
        return zonedDateTimeOptional.map(zonedTime -> zonedTime.withZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime());
    }

    /**
     * Get a OffsetDateTime object (optional) that represents the time for next execution by given cron  and timeZone
     * from now
     * @param cron cron object
     * @param zoneId timezone
     * @return an optional ZonedDateTime that represents the time for next execution
     */
    public static Optional<OffsetDateTime> getNextUTCExecutionTimeFromNow(Cron cron, String zoneId) {
        return getNextUTCExecutionTime(cron,DateTimeUtils.now(),zoneId);
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

    public static OffsetDateTime getUTCExecutionTimeForSpecificTimeCron(String cronExpr, String zoneIdString) {
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(zoneIdString);
        } catch (DateTimeException ex) {
            throw new IllegalArgumentException("Zond Id is invalid");
        }
        validateCron(CronUtils.convertStringToCron(cronExpr));
        String[] cronFields = cronExpr.split(" ");
        Preconditions.checkArgument(validateCronIsSpecificTime(cronFields), "Cron expression should be a specific time");
        OffsetDateTime result = OffsetDateTime.of(Integer.parseInt(cronFields[6]),
                Integer.parseInt(cronFields[4]),
                Integer.parseInt(cronFields[3]),
                Integer.parseInt(cronFields[2]),
                Integer.parseInt(cronFields[1]),
                Integer.parseInt(cronFields[0]),
                0,
                zoneId.getRules().getOffset(Instant.now()));
        return OffsetDateTime.ofInstant(result.toInstant(), ZoneOffset.UTC);
    }

    private static boolean validateCronIsSpecificTime(String[] cronFields) {
        for(int i = 0; i < cronFields.length; i++) {
            if (i == 5) {
                if (!cronFields[i].equals("?")) {
                    return false;
                }
            } else {
                try {
                    Integer value = Integer.parseInt(cronFields[i]);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        return true;
    }

}
