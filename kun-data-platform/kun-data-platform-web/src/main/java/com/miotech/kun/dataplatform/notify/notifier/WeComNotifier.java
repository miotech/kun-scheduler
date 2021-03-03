package com.miotech.kun.dataplatform.notify.notifier;

import com.miotech.kun.dataplatform.notify.MessageNotifier;
import com.miotech.kun.dataplatform.notify.service.WeComService;
import com.miotech.kun.dataplatform.notify.userconfig.WeComNotifierUserConfig;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;


public class WeComNotifier implements MessageNotifier {
    private final WeComService weComService;

    private final WeComNotifierUserConfig userConfig;

    public WeComNotifier(final WeComService weComService, final WeComNotifierUserConfig userConfig) {
        this.weComService = weComService;
        this.userConfig = userConfig;
    }

    @Override
    public void notify(Event event) {
        if (event instanceof TaskAttemptStatusChangeEvent) {
            weComService.sendMessage((TaskAttemptStatusChangeEvent) event);
        }
        // else: do not handle
    }
}
