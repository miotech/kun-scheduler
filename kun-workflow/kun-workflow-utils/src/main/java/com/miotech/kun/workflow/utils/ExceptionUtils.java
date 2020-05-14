package com.miotech.kun.workflow.utils;

import static com.google.common.base.Preconditions.checkNotNull;

public class ExceptionUtils {
    // util class should not be instantiated
    private ExceptionUtils() {
    }

    public static RuntimeException wrapIfChecked(Throwable throwable) {
        checkNotNull(throwable, "throwable should not be null.");
        if (throwable instanceof RuntimeException) {
            return (RuntimeException) throwable;
        }
        return new RuntimeException(throwable);
    }
}
