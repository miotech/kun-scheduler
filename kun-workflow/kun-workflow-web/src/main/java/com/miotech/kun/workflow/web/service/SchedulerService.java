package com.miotech.kun.workflow.web.service;

import com.google.inject.Inject;
import com.miotech.kun.commons.utils.InitializingBean;
import com.miotech.kun.workflow.SchedulerManager;

public class SchedulerService implements InitializingBean {

    @Inject
    private SchedulerManager schedulerManager;

    @Override
    public void afterPropertiesSet() {
        schedulerManager.start();
    }

    @Override
    public Order getOrder() {
        return Order.LAST;
    }
}
