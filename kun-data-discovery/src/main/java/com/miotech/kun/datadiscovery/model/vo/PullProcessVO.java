package com.miotech.kun.datadiscovery.model.vo;

import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author: Jie Chen
 * @created: 2020/7/1
 */
@Data
public class PullProcessVO {
    String processId;
    TaskRunStatus status;
    String pullCreator;
    OffsetDateTime pullStartAt;
    OffsetDateTime pullEndAt;
}
