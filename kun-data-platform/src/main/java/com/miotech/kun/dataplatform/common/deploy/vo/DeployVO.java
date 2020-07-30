package com.miotech.kun.dataplatform.common.deploy.vo;

import com.miotech.kun.dataplatform.model.deploy.DeployCommit;
import com.miotech.kun.dataplatform.model.deploy.DeployStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class DeployVO {
    private final Long id;

    private final String name;

    private final List<DeployCommit> commits;

    private final Long creator;

    private final OffsetDateTime submittedAt;

    private final Long deployer;

    private final OffsetDateTime deployedAt;

    private final DeployStatus status;

}
