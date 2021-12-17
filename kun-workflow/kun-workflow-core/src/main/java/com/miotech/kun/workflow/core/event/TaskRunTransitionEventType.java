package com.miotech.kun.workflow.core.event;

public enum TaskRunTransitionEventType {
    SUBMIT,
    HANGUP,
    UPSTREAM_FAILED,
    RUNNING,
    ABORT,
    RESCHEDULE,
    AWAKE,
    CHECK,
    FAILED,
    CHECK_SUCCESS,
    CHECK_FAILED,
    EXCEPTION


}
