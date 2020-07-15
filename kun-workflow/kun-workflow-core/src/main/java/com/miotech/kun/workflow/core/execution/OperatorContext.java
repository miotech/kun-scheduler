package com.miotech.kun.workflow.core.execution;

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
}
