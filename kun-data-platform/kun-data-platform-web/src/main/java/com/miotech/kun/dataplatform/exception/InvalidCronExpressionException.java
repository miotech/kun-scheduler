package com.miotech.kun.dataplatform.exception;

public class InvalidCronExpressionException extends DataPlatformBaseException {
    public InvalidCronExpressionException(String cronExpression) {
        super(400, String.format("Invalid cron expression: \"%s\". Please re-check and amend.", cronExpression));
    }
}
