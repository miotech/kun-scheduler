package com.miotech.kun.monitor.facade.model.alert;

import com.miotech.kun.commons.pubsub.event.Event;

import java.util.List;

/**
 * An abstract class represents notification configuration which holds user-defined configurations
 * and a test rule which judges whether notifications should be sent or not.
 */
public abstract class NotifyConfig {
    /**
     * Test if a event should be notified
     * @param event the event object to be tested
     * @return {true} if notification should be send. {false} if not.
     */
    public abstract boolean test(Event event);

    /**
     * Get user configurations of applied notifiers
     */
    public abstract List<NotifierUserConfig> getNotifierConfigs();
}
