package com.miotech.kun.workflow.core.execution;

import com.miotech.kun.workflow.core.annotation.Internal;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class KunOperator {
    private OperatorContext context;
    private TaskAttemptReport report;

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
     * Task被终止时调用的函数。
     * 因为和Task执行的线程不是同一个线程，所以需要确保Operator的线程安全性。
     */
    public abstract void abort();

    /**
     * 获得Operator的配置项定义。
     */
    public abstract ConfigDef config();

    /**
     * 上报任务执行报告
     * @param report
     */
    protected void report(TaskAttemptReport report) {
        this.report = report;
    }

    /**
     * 获取任务执行报告
     * @return
     */
    @Internal
    public Optional<TaskAttemptReport> getReport() {
        return Optional.ofNullable(report);
    }

    /**
     * 获得OperatorContext。
     */
    protected OperatorContext getContext() {
        checkNotNull(context, "context should not be null.");
        return context;
    }

    @Internal
    public void setContext(OperatorContext context) {
        this.context = context;
    }

}
