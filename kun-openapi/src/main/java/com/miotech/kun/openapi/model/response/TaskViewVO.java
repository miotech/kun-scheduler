package com.miotech.kun.openapi.model.response;

import com.miotech.kun.dataplatform.web.common.taskdefview.vo.TaskDefinitionViewVO;
import com.miotech.kun.dataplatform.web.model.taskdefview.TaskDefinitionView;
import lombok.Data;

@Data
public class TaskViewVO {
    private final Long id;
    private final String taskViewName;

    public static TaskViewVO from(TaskDefinitionView taskDefinitionView) {
        return new TaskViewVO(taskDefinitionView.getId(), taskDefinitionView.getName());
    }

    public static TaskViewVO from(TaskDefinitionViewVO taskDefinitionViewVO) {
        return new TaskViewVO(taskDefinitionViewVO.getId(), taskDefinitionViewVO.getName());
    }
}
