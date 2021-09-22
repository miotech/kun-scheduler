package com.miotech.kun.monitor.alert.notifier;

import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.monitor.alert.service.WeComService;
import com.miotech.kun.monitor.facade.model.alert.WeComNotifierUserConfig;

public class WeComNotifier implements MessageNotifier {
    private final WeComService weComService;

    private final WeComNotifierUserConfig userConfig;

    public WeComNotifier(final WeComService weComService, final WeComNotifierUserConfig userConfig) {
        this.weComService = weComService;
        this.userConfig = userConfig;
    }

    @Override
    public void notify(Long workflowTaskId, String subject, String message) {
        weComService.sendMessage(workflowTaskId, message);
    }

    @Override
    public void notifyTaskStatusChange(Event event) {
        weComService.sendMessage(event);
    }
}
