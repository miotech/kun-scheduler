package com.miotech.kun.dataplatform.common.commit.vo;

import com.miotech.kun.dataplatform.model.commit.CommitStatus;
import com.miotech.kun.dataplatform.model.commit.CommitType;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class TaskCommitVO {
    private final Long id;

    private final Long definitionId;

    private final String version;

    private final String message;

    private final Long committer;

    private final OffsetDateTime committedAt;

    private final CommitType commitType;

    private final CommitStatus commitStatus;

    private final boolean latestCommit;
}
