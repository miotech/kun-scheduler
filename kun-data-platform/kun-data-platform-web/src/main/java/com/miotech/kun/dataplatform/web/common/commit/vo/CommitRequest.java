package com.miotech.kun.dataplatform.web.common.commit.vo;

import lombok.Data;

@Data
public class CommitRequest {
    private final Long definitionId;

    private final String message;
}
