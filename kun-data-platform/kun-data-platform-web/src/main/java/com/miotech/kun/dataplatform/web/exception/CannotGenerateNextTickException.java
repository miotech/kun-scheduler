package com.miotech.kun.dataplatform.web.exception;

public class CannotGenerateNextTickException extends DataPlatformBaseException {
    public CannotGenerateNextTickException(String cronExpression) {
        super(400, String.format("Cannot generate next tick from Quartz cron expression: %s", cronExpression));
    }
}
