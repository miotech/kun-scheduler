package com.miotech.kun.dataplatform.web.common.commit.vo;

import com.miotech.kun.dataplatform.facade.model.commit.CommitStatus;
import com.miotech.kun.dataplatform.facade.model.commit.CommitType;
import com.miotech.kun.dataplatform.facade.model.commit.TaskSnapshot;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class TaskCommitVO {
    private final Long id;

    private final Long definitionId;

    private final String version;

    private final String message;

    private final TaskSnapshot snapshot;

    private final String committer;

    private final OffsetDateTime committedAt;

    private final CommitType commitType;

    private final CommitStatus commitStatus;

    private final boolean latestCommit;
}
