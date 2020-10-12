package com.miotech.kun.workflow.common.exception;

public class ResolverUndefinedException extends IllegalArgumentException {
    public ResolverUndefinedException() {
    }

    public ResolverUndefinedException(String message) {
        super(message);
    }
}
