package com.miotech.kun.datadashboard.model.bo;

import com.miotech.kun.common.model.PageInfo;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: Jie Chen
 * @created: 2020/12/8
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class DataDevelopmentTasksRequest extends PageInfo {

    TaskRunStatus taskRunStatus;

    Boolean includeStartedOnly;
}
