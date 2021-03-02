package com.miotech.kun.dataplatform.notify.notifier;

import com.miotech.kun.dataplatform.notify.MessageNotifier;
import com.miotech.kun.dataplatform.notify.service.ZhongdaService;
import com.miotech.kun.dataplatform.notify.userconfig.ZhongdaNotifierUserConfig;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;

public class ZhongdaNotifier implements MessageNotifier {
    private final ZhongdaService zhongdaService;

    private final ZhongdaNotifierUserConfig userConfig;

    public ZhongdaNotifier(final ZhongdaService zhongdaService, final ZhongdaNotifierUserConfig userConfig) {
        this.zhongdaService = zhongdaService;
        this.userConfig = userConfig;
    }

    @Override
    public void notify(Event event) {
        if (event instanceof TaskAttemptStatusChangeEvent) {
            zhongdaService.sendMessage((TaskAttemptStatusChangeEvent) event);
        }
        // else: do not handle
    }
}
