package com.miotech.kun.dataplatform.web.common.deploy.vo;

import com.miotech.kun.dataplatform.facade.model.deploy.DeployCommit;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployStatus;
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
