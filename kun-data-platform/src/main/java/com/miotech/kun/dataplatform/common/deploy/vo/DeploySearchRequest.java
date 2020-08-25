package com.miotech.kun.dataplatform.common.deploy.vo;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(callSuper = false)
@Data
public class DeploySearchRequest extends PageRequest {
    private List<Long> creatorIds;

    private Optional<OffsetDateTime> submittedAtTo;

    private Optional<OffsetDateTime> submittedAtFrom;

    private List<Long> deployerIds;

    private Optional<OffsetDateTime> depoyedAtFrom;

    private Optional<OffsetDateTime> depoyedAtTo;

    public DeploySearchRequest(int pageSize,
                               int pageNum,
                               List<Long> creatorIds,
                               Optional<OffsetDateTime> submittedAtTo,
                               Optional<OffsetDateTime> submittedAtFrom,
                               List<Long> deployerIds,
                               Optional<OffsetDateTime> depoyedAtFrom,
                               Optional<OffsetDateTime> depoyedAtTo) {
        super(pageSize, pageNum);
        this.creatorIds = creatorIds != null ? creatorIds : ImmutableList.of();
        this.submittedAtTo = submittedAtTo;
        this.submittedAtFrom = submittedAtFrom;
        this.deployerIds = deployerIds != null ? deployerIds : ImmutableList.of();
        this.depoyedAtFrom = depoyedAtFrom;
        this.depoyedAtTo = depoyedAtTo;
    }
}
