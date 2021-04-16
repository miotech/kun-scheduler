package com.miotech.kun.dataplatform.notify.service;

import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class WeComService {

    // TODO: Currently we are using zhongda (an internal service of Miotech) to send WeCom alerts.
    //       Later on we shall replace this implementation by using WeCom official APIs directly
    @Autowired
    private ZhongdaService zhongdaService;

    public void sendMessage(Event event) {
        if (event instanceof TaskAttemptStatusChangeEvent) {
            zhongdaService.sendMessage((TaskAttemptStatusChangeEvent) event);
        }
    }
}
