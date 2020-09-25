package com.miotech.kun.dataplatform.common.commit.vo;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(callSuper = false)
@Data
public class CommitSearchRequest extends PageRequest {
    private List<Long> commiterIds;

    private List<Long> definitionIds;

    private Optional<Boolean> isLatest;

    public CommitSearchRequest(int pageSize,
                               int pageNum,
                               List<Long> commiterIds,
                               List<Long> definitionIds,
                               Optional<Boolean> isLatest) {
        super(pageSize, pageNum);
        this.commiterIds = commiterIds != null ? commiterIds : ImmutableList.of();
        this.definitionIds = definitionIds != null ? definitionIds : ImmutableList.of();
        this.isLatest = isLatest;
    }
}
