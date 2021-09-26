package com.miotech.kun.dataplatform.web.exception;

public class BackfillTooManyTasksException extends DataPlatformBaseException {
    public BackfillTooManyTasksException() {
        super(400, "Cannot create a single backfill batch with more than 100 tasks.");
    }
}
