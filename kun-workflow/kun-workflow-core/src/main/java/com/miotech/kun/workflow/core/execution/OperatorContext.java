package com.miotech.kun.workflow.core.execution;

import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;
import com.miotech.kun.workflow.core.resource.Resource;

public interface OperatorContext {
    /**
     * 获取资源，用于支持用户上传jar包类型的任务。
     */
    public Resource getResource(String path);

    /**
     * 获取任务的配置信息
     * @return
     */
    public Config getConfig();

    /**
     * 获取任务运行实例ID
     */
    public Long getTaskRunId();

    /**
     * 获取任务调度时间
     */
    public String getScheduleTime();

    /**
     * 获取任务执行环境信息
     */
    public ExecuteTarget getExecuteTarget();

    /**
     * 获取任务队列信息
     */
    public String getQueueName();
}
