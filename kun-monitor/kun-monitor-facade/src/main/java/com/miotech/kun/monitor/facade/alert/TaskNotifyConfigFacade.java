package com.miotech.kun.monitor.facade.alert;

import com.miotech.kun.monitor.facade.model.alert.TaskDefNotifyConfig;

public interface TaskNotifyConfigFacade {

    void updateRelatedTaskNotificationConfig(Long workflowTaskId, TaskDefNotifyConfig taskDefNotifyConfig);

}
