package com.miotech.kun.dataquality.web.service;

import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TaskAttemptFinishedEventHandlerManager {

    private List<TaskAttemptFinishedEventHandler> handlers;

    @Autowired
    public TaskAttemptFinishedEventHandlerManager(List<TaskAttemptFinishedEventHandler> handlers) {
        this.handlers = handlers;
    }

    public void handle(TaskAttemptFinishedEvent taskAttemptFinishedEvent) {
        if (CollectionUtils.isEmpty(handlers)) {
            return;
        }

        for (TaskAttemptFinishedEventHandler handler : handlers) {
            try {
                log.info("TaskAttemptFinishedEventHandler: {} begin execution", handler.getClass().getName());
                handler.handle(taskAttemptFinishedEvent);
            } catch (Throwable t) {
                String msg = String.format("Handle TaskAttemptFinishedEvent failed, taskAttemptFinishedEvent: %s, handler: %s",
                        taskAttemptFinishedEvent, handler.getClass().getName());
                log.error(msg, t);
            }
        }
    }

}
