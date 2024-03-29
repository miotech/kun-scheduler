package com.miotech.kun.dataplatform.web.common.deploy.vo;

import lombok.Data;

import java.util.List;

@Data
public class DeployedTaskDAG {
    private final List<DeployedTaskVO> nodes;

    private final List<DeployedTaskDependencyVO> edges;

}
