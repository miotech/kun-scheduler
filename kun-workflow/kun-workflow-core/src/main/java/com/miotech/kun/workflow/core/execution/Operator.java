package com.miotech.kun.workflow.core.execution;

import com.miotech.kun.workflow.core.annotation.Internal;

public abstract class Operator {
    private OperatorContext context;

    /**
     * 初始化Operator。
     */
    public void init() {
        // do nothing
    }

    /**
     * 运行Operator。
     */
    public abstract boolean run();

    /**
     * 获得OperatorContext。
     */
    protected OperatorContext getContext() {
        return context;
    }

    @Internal
    public void setContext(OperatorContext context) {
        this.context = context;
    }

    /**
     * Task被终止时调用的函数。
     * 因为和Task执行的线程不是同一个线程，所以需要确保Operator的线程安全性。
     */
    public void onAbort() {
        // do nothing
    }
}
