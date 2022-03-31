package com.miotech.kun.dataquality.web.service;

import com.miotech.kun.commons.pubsub.event.Event;
import com.miotech.kun.workflow.core.event.TaskAttemptFinishedEvent;
import com.miotech.kun.workflow.core.event.TaskAttemptStatusChangeEvent;
import com.miotech.kun.workflow.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class EventHandlerManager {

    private List<TaskAttemptFinishedEventHandler> finishedEventHandlers;
    private List<TaskAttemptStatusChangeEventHandler> statusChangeEventHandlers;

    @Autowired
    public EventHandlerManager(List<TaskAttemptFinishedEventHandler> finishedEventHandlers,
                               List<TaskAttemptStatusChangeEventHandler> statusChangeEventHandlers) {
        this.finishedEventHandlers = finishedEventHandlers;
        this.statusChangeEventHandlers = statusChangeEventHandlers;
    }

    public void handle(Event event) {
        if (event instanceof TaskAttemptFinishedEvent) {
            TaskAttemptFinishedEvent taskAttemptFinishedEvent = (TaskAttemptFinishedEvent) event;
            finishedEventHandlers.stream().forEach(handler -> {
                try {
                    log.info("TaskAttemptFinishedEventHandler: {} begin execution, taskAttemptFinishedEvent: {}",
                            handler.getClass().getName(), JSONUtils.toJsonString(taskAttemptFinishedEvent));
                    handler.handle(taskAttemptFinishedEvent);
                } catch (Throwable t) {
                    String msg = String.format("Handle TaskAttemptFinishedEvent failed, taskAttemptFinishedEvent: %s, handler: %s",
                            JSONUtils.toJsonString(taskAttemptFinishedEvent), handler.getClass().getName());
                    log.error(msg, t);
                }
            });
        }

        if (event instanceof TaskAttemptStatusChangeEvent) {
            TaskAttemptStatusChangeEvent taskAttemptStatusChangeEvent = (TaskAttemptStatusChangeEvent) event;
            statusChangeEventHandlers.stream().forEach(handler -> {
                try {
                    log.info("TaskAttemptStatusChangeEventHandler: {} begin execution, taskAttemptStatusChangeEvent: {}",
                            handler.getClass().getName(), JSONUtils.toJsonString(taskAttemptStatusChangeEvent));
                    handler.handle(taskAttemptStatusChangeEvent);
                } catch (Throwable t) {
                    String msg = String.format("Handle TaskAttemptStatusChangeEvent failed, taskAttemptStatusChangeEvent: %s, handler: %s",
                            JSONUtils.toJsonString(taskAttemptStatusChangeEvent), handler.getClass().getName());
                    log.error(msg, t);
                }
            });
        }
    }

}
