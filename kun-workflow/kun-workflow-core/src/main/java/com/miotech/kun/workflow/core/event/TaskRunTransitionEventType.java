package com.miotech.kun.workflow.core.event;

public enum TaskRunTransitionEventType {
    ASSEMBLED,
    SUBMIT,
    HANGUP,
    WAIT,
    UPSTREAM_FAILED,
    CONDITION_CHANGE,
    CONDITION_REMOVE,
    READY,
    ABORT,
    RECOVER,
    RESCHEDULE,
    RESET,
    AWAKE,
    COMPLETE,
    CHECK,
    FAILED,
    CHECK_SUCCESS,
    CHECK_FAILED,
    EXCEPTION,
    SKIP,
    RESUBMIT//attempt failed auto retry


}
