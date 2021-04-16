package com.miotech.kun.workflow.web.service;

import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseSetup;
import com.miotech.kun.commons.rpc.RpcPublisher;
import com.miotech.kun.commons.utils.InitializingBean;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.facade.WorkflowExecutorFacade;

import javax.sql.DataSource;

public class InitService implements InitializingBean {
    @Inject
    private Props props;
    @Inject
    private DataSource dataSource;
    @Inject
    private RpcPublisher rpcPublisher;
    @Inject
    private WorkflowExecutorFacade executorFacade;


    @Override
    public void afterPropertiesSet() {
        configureDB();
        publishRpcServices();
    }

    private void configureDB() {
        DatabaseSetup setup = new DatabaseSetup(dataSource, props);
        setup.start();
    }

    private void publishRpcServices() {
        if (props.containsKey("rpc.registry")) {
            rpcPublisher.exportService(WorkflowExecutorFacade.class, "1.0", executorFacade);
        }
    }

    @Override
    public Order getOrder() {
        return Order.FIRST;
    }
}
