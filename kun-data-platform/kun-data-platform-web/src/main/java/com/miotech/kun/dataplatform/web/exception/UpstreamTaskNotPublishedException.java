package com.miotech.kun.dataplatform.web.exception;

public class UpstreamTaskNotPublishedException extends DataPlatformBaseException {
    public UpstreamTaskNotPublishedException(String message) {
        super(409, message);
    }
}
