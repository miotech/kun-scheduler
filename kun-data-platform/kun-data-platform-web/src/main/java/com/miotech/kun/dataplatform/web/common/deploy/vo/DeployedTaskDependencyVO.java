package com.miotech.kun.dataplatform.web.common.deploy.vo;

import lombok.Data;

@Data
public class DeployedTaskDependencyVO {
    private final Long downStreamTaskId;

    private final Long upstreamTaskId;

}