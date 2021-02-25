package com.miotech.kun.dataplatform.model.notify;

import com.miotech.kun.dataplatform.notify.userconfig.NotifierUserConfig;
import com.miotech.kun.workflow.core.event.Event;

import java.util.List;

/**
 * An abstract class represents notification configuration which holds user-defined configurations
 * and a test rule which judges whether notifications should be sent or not.
 */
public abstract class NotifyConfig {
    private List<NotifierUserConfig> notifierConfigs;

    /**
     * Test if a event should be notified
     * @param event the event object to be tested
     * @return {true} if notification should be send. {false} if not.
     */
    public abstract boolean test(Event event);

    /**
     * Get user configurations of applied notifiers
     */
    public List<NotifierUserConfig> getNotifierConfigs() {
        return this.notifierConfigs;
    }
}
