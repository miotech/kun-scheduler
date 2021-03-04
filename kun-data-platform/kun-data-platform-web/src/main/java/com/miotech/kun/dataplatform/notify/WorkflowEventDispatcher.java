package com.miotech.kun.dataplatform.notify;

import com.miotech.kun.dataplatform.common.notifyconfig.service.TaskNotifyConfigService;
import com.miotech.kun.dataplatform.model.notify.TaskNotifyConfig;
import com.miotech.kun.dataplatform.notify.notifier.EmailNotifier;
import com.miotech.kun.dataplatform.notify.notifier.WeComNotifier;
import com.miotech.kun.dataplatform.notify.service.EmailService;
import com.miotech.kun.dataplatform.notify.service.WeComService;
import com.miotech.kun.dataplatform.notify.userconfig.EmailNotifierUserConfig;
import com.miotech.kun.dataplatform.notify.userconfig.NotifierUserConfig;
import com.miotech.kun.dataplatform.notify.userconfig.WeComNotifierUserConfig;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.core.publish.EventSubscriber;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class WorkflowEventDispatcher {
    @Autowired
    private EventSubscriber workflowEventSubscriber;

    @Autowired
    private WeComService weComService;

    @Autowired
    private EmailService emailService;

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
        // 2. Is there any task notify config relates to this task id?
        Optional<TaskNotifyConfig> taskNotifyConfigOptional = taskNotifyConfigService.fetchTaskNotifyConfigByWorkflowTaskId(workflowTaskId);
        if (taskNotifyConfigOptional.isPresent()) {
            // 3. If there is, read the configuration and notify by notifiers
            TaskNotifyConfig notifyConfig = taskNotifyConfigOptional.get();
            // 4. Does this change event match configuration trigger type?
            if (notifyConfig.test(statusChangeEvent)) {
                // 5. If matches, construct notifiers
                List<MessageNotifier> notifiers = constructNotifiersFromUserConfig(notifyConfig.getNotifierConfigs());
                // 6. Notify by each notifier
                notifiers.forEach(notifier -> notifier.notify(statusChangeEvent));
            }
        }
    }

    private List<MessageNotifier> constructNotifiersFromUserConfig(List<NotifierUserConfig> userConfigs) {
        List<MessageNotifier> notifiers = new ArrayList<>(userConfigs.size());
        for (NotifierUserConfig userConfig : userConfigs) {
            switch (userConfig.getNotifierType()) {
                case "EMAIL":
                    EmailNotifier emailNotifier = new EmailNotifier(emailService, (EmailNotifierUserConfig) userConfig);
                    notifiers.add(emailNotifier);
                    break;
                case "WECOM":
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
