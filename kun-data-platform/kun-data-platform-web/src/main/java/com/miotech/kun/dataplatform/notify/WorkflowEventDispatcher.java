package com.miotech.kun.dataplatform.notify;

import com.miotech.kun.dataplatform.common.notifyconfig.service.TaskNotifyConfigService;
import com.miotech.kun.dataplatform.constant.NotifierTypeNameConstants;
import com.miotech.kun.dataplatform.model.notify.NotifyConfig;
import com.miotech.kun.dataplatform.model.notify.TaskNotifyConfig;
import com.miotech.kun.dataplatform.model.notify.TaskStatusNotifyTrigger;
import com.miotech.kun.dataplatform.notify.notifier.EmailNotifier;
import com.miotech.kun.dataplatform.notify.notifier.WeComNotifier;
import com.miotech.kun.dataplatform.notify.service.EmailService;
import com.miotech.kun.dataplatform.notify.service.WeComService;
import com.miotech.kun.dataplatform.notify.userconfig.EmailNotifierUserConfig;
import com.miotech.kun.dataplatform.notify.userconfig.NotifierUserConfig;
import com.miotech.kun.dataplatform.notify.userconfig.WeComNotifierUserConfig;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.publish.EventSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Component
public class WorkflowEventDispatcher {
    @Autowired
    private EventSubscriber workflowEventSubscriber;

    @Autowired
    private WeComService weComService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SystemDefaultNotifierConfig systemDefaultNotifierConfig;

    @Autowired
    private TaskNotifyConfigService taskNotifyConfigService;

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
        // 2. Is there any task notify config relates to this task id?
        Optional<TaskNotifyConfig> taskNotifyConfigOptional = taskNotifyConfigService.fetchTaskNotifyConfigByWorkflowTaskId(workflowTaskId);

        TaskNotifyConfig notifyConfig;
        if (taskNotifyConfigOptional.isPresent()) {
            log.debug("`taskNotifyConfig` exists, value = {}", taskNotifyConfigOptional.get());
            // 3. If there is, read the configuration and notify by notifiers
            notifyConfig = taskNotifyConfigOptional.get();
        } else {
            // Use system default notify configuration
            log.debug("Cannot find `taskNotifyConfig` for workflow task id = {}. Using system default config.", workflowTaskId);
            notifyConfig = TaskNotifyConfig.newBuilder()
                    .withWorkflowTaskId(workflowTaskId)
                    .withNotifierConfigs(Collections.emptyList())
                    .withTriggerType(TaskStatusNotifyTrigger.SYSTEM_DEFAULT)
                    .build();
        }

        // 4. Does this change event match configuration trigger type?
        boolean matchFlag;
        if (Objects.equals(notifyConfig.getTriggerType(), TaskStatusNotifyTrigger.SYSTEM_DEFAULT)) {
            matchFlag = systemDefaultNotifierConfig.getSystemDefaultTriggerType().matches(statusChangeEvent.getToStatus());
        } else {
            matchFlag = notifyConfig.test(statusChangeEvent);
        }
        log.debug("`matchFlag` of current event = {}.", matchFlag);

        // 5. If matches, construct notifiers
        if (matchFlag) {
            List<MessageNotifier> notifiers = constructNotifiersFromNotifyConfig(notifyConfig);
            log.debug("`notifiers` of current event = {}.", notifiers);
            // 6. Notify by each notifier
            notifiers.forEach(notifier -> notifier.notify(statusChangeEvent));
        }
    }

    private List<MessageNotifier> constructNotifiersFromNotifyConfig(NotifyConfig notifyConfig) {
        List<NotifierUserConfig> userConfigs;
        if ((notifyConfig instanceof TaskNotifyConfig) && Objects.equals(((TaskNotifyConfig) notifyConfig).getTriggerType(), TaskStatusNotifyTrigger.SYSTEM_DEFAULT)) {
            // special case: use system default config when trigger type is SYSTEM_DEFAULT
            userConfigs = systemDefaultNotifierConfig.getSystemDefaultConfig();
        } else {
            userConfigs = notifyConfig.getNotifierConfigs();
        }
        List<MessageNotifier> notifiers = new ArrayList<>(userConfigs.size());
        for (NotifierUserConfig userConfig : userConfigs) {
            switch (userConfig.getNotifierType()) {
                case NotifierTypeNameConstants.EMAIL:
                    EmailNotifier emailNotifier = new EmailNotifier(emailService, (EmailNotifierUserConfig) userConfig);
                    notifiers.add(emailNotifier);
                    break;
                case NotifierTypeNameConstants.WECOM:
                    WeComNotifier weComNotifier = new WeComNotifier(weComService, (WeComNotifierUserConfig) userConfig);
                    notifiers.add(weComNotifier);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Unknown user notification config with notifier type = \"%s\"", userConfig.getNotifierType()));
            }
        }
        return notifiers;
    }
}
