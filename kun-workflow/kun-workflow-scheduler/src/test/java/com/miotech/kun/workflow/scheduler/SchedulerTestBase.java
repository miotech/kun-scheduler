package com.miotech.kun.workflow.scheduler;

import com.google.common.eventbus.EventBus;
import com.miotech.kun.commons.testing.DatabaseTestBase;

public class SchedulerTestBase extends DatabaseTestBase {
    @Override
    protected void configuration() {
        super.configuration();
        bind(EventBus.class, new EventBus());
    }
}
