package com.miotech.kun.monitor.alert.dispatcher;

import com.miotech.kun.common.constant.DataQualityConstant;
import com.miotech.kun.commons.pubsub.subscribe.EventSubscriber;
import com.miotech.kun.dataplatform.facade.BackfillFacade;
import com.miotech.kun.dataplatform.facade.TaskDefinitionFacade;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskTry;
import com.miotech.kun.monitor.alert.common.service.TaskNotifyConfigService;
import com.miotech.kun.monitor.alert.service.EmailService;
import com.miotech.kun.monitor.alert.service.NotifyService;
import com.miotech.kun.monitor.alert.service.WeComService;
import com.miotech.kun.monitor.facade.model.alert.SystemDefaultNotifierConfig;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Slf4j
@Component
public class TaskAttemptStatusChangeEventDispatcher {
    @Autowired
    @Qualifier("alert-subscriber")
    private EventSubscriber workflowEventSubscriber;

    @Autowired(required = false)
    private WeComService weComService;

    @Autowired(required = false)
    private EmailService emailService;

    @Autowired
    private SystemDefaultNotifierConfig systemDefaultNotifierConfig;

    @Autowired
    private TaskNotifyConfigService taskNotifyConfigService;

    @Autowired
    private BackfillFacade backfillFacade;

    @Autowired
    private TaskDefinitionFacade taskDefinitionFacade;

    @Autowired
    private NotifyService notifyService;

    @PostConstruct
    private void onDispatcherConstructed() {
        doSubscribe();
    }

    private void doSubscribe() {
        workflowEventSubscriber.subscribe(event -> {
            if (event instanceof TaskAttemptStatusChangeEvent) {
                handleTaskAttemptStatusChangeEvent((TaskAttemptStatusChangeEvent) event);
            }
        });
    }

    private void handleTaskAttemptStatusChangeEvent(TaskAttemptStatusChangeEvent statusChangeEvent) {
        // 1. Read task id from event payload
        Long workflowTaskId = statusChangeEvent.getTaskId();
        log.debug("Receiving `TaskAttemptStatusChangeEvent` with workflow task id = {}", workflowTaskId);

        // Filter out `backfill`、`dry-run`、`data-quailty` task
        if (filter(statusChangeEvent)) {
            return;
        }

        notifyService.notify(workflowTaskId, statusChangeEvent, null, null, false, statusChangeEvent.getToStatus());
    }

    private boolean filter(TaskAttemptStatusChangeEvent statusChangeEvent) {
        if (statusChangeEvent.getTaskName().startsWith(DataQualityConstant.WORKFLOW_TASK_NAME_PREFIX)) {
            return true;
        }

        Optional<Long> backfillIdOpt = backfillFacade.findDerivedFromBackfill(statusChangeEvent.getTaskRunId());
        if (backfillIdOpt.isPresent()) {
            return true;
        }

        Optional<TaskTry> taskTryOpt = taskDefinitionFacade.findTaskTryByTaskRunId(statusChangeEvent.getTaskRunId());
        if (taskTryOpt.isPresent()) {
            return true;
        }

        return false;
    }

}
