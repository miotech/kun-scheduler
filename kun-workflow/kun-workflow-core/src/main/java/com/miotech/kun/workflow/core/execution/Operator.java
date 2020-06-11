package com.miotech.kun.workflow.core.execution;

public interface Operator {
    /**
     * 初始化Operator。
     */
    public void init(OperatorContext context);

    /**
     * 运行Operator。
     */
    public boolean run(OperatorContext context);

    /**
     * Task被终止时调用的函数。
     * 因为和Task执行的线程不是同一个线程，所以需要确保Operator的线程安全性。
     */
    public void onAbort(OperatorContext context);
}
