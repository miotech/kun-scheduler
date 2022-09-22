package com.miotech.kun.workflow.web.service;

import com.google.inject.Inject;
import com.miotech.kun.commons.utils.InitializingBean;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.SchedulerManager;
import com.miotech.kun.workflow.TaskRunStateMachineDispatcher;
import com.miotech.kun.workflow.core.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecoverService implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(RecoverService.class);

    @Inject
    private Props props;

    @Inject
    private Executor executor;

    @Inject
    private SchedulerManager schedulerManager;

    @Inject
    private TaskRunStateMachineDispatcher stateMachineDispatcher;

    @Override
    public void afterPropertiesSet() {
        logger.info("Recover Service running...");
        stateMachineDispatcher.recover();
        if (props.getBoolean("executor.enableRecover", true)) {
            executor.recover();
        }
        schedulerManager.start();
    }

    @Override
    public Order getOrder() {
        return Order.NORMAL;
    }
}
