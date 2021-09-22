package com.miotech.kun.monitor.alert.service;

import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.monitor.alert.notifier.MessageNotifier;
import com.miotech.kun.monitor.facade.alert.NotifyFacade;
import com.miotech.kun.monitor.facade.model.alert.SystemDefaultNotifierConfig;
import com.miotech.kun.monitor.alert.common.service.TaskNotifyConfigService;
import com.miotech.kun.monitor.alert.constant.NotifierTypeNameConstants;
import com.miotech.kun.monitor.facade.model.alert.*;
import com.miotech.kun.monitor.alert.notifier.EmailNotifier;
import com.miotech.kun.monitor.alert.notifier.WeComNotifier;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class NotifyService implements NotifyFacade {

    private static final String EMAIL_SUBJECT = "SLA ALERT";

    @Autowired(required = false)
    private WeComService weComService;

    @Autowired(required = false)
    private EmailService emailService;

    @Autowired
    private TaskNotifyConfigService taskNotifyConfigService;

    @Autowired
    private SystemDefaultNotifierConfig systemDefaultNotifierConfig;

    public void notify(Long workflowTaskId, String msg) {
        notify(workflowTaskId, null, msg, true, null);
    }

    public void notify(Long workflowTaskId, Event event, String msg, boolean matchIgnore, TaskRunStatus taskRunStatus) {
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

        List<MessageNotifier> notifiers = constructNotifiersFromNotifyConfig(notifyConfig);
        if (matchIgnore) {
            log.debug("`notifiers` of current event = {}.", notifiers);
            // 6. Notify by each notifier
            notifiers.forEach(notifier -> notifier.notify(workflowTaskId, EMAIL_SUBJECT, msg));
            return;
        }

        // 4. Does this change event match configuration trigger type?
        boolean matchFlag;
        if (Objects.equals(notifyConfig.getTriggerType(), TaskStatusNotifyTrigger.SYSTEM_DEFAULT)) {
            matchFlag = systemDefaultNotifierConfig.getSystemDefaultTriggerType().matches(taskRunStatus);
        } else {
            matchFlag = notifyConfig.test(event);
        }

        // 5. If matches, construct notifiers
        if (matchFlag) {
            log.debug("`notifiers` of current event = {}.", notifiers);
            // 6. Notify by each notifier
            notifiers.forEach(notifier -> notifier.notifyTaskStatusChange(event));
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
