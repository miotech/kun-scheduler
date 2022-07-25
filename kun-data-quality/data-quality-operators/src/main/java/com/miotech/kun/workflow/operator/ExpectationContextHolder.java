package com.miotech.kun.workflow.operator;

import com.miotech.kun.workflow.core.execution.OperatorContext;

public class ExpectationContextHolder {

    private static final ThreadLocal<OperatorContext> OPERATOR_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    public static void setContext(OperatorContext context) {
        OPERATOR_CONTEXT_THREAD_LOCAL.set(context);
    }

    public static OperatorContext getContext() {
        return OPERATOR_CONTEXT_THREAD_LOCAL.get();
    }

    public static void remove() {
        OPERATOR_CONTEXT_THREAD_LOCAL.remove();
    }

}
