package com.miotech.kun.infra.service;

import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.DatabaseSetup;
import com.miotech.kun.commons.utils.InitializingBean;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.infra.util.MetadataSysTaskBuilder;
import com.miotech.kun.metadata.web.constant.OperatorParam;
import com.miotech.kun.metadata.web.constant.TaskParam;
import com.miotech.kun.metadata.web.processor.MetadataConsumerStarter;
import com.miotech.kun.workflow.common.rpc.ExecutorServer;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.facade.WorkflowServiceFacade;
import com.miotech.kun.workflow.common.rpc.ExecutorFacadeImpl;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InitService implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(com.miotech.kun.infra.service.InitService.class);

    @Inject
    private WorkflowServiceFacade workflowServiceFacade;

    @Inject
    private Props props;

    @Inject
    private javax.sql.DataSource dataSource;

    @Inject
    private MetadataConsumerStarter metadataConsumerStarter;

    @Inject
    private DatabaseOperator dbOperator;

    @Inject
    private OperatorUploadService operatorUploadService;

    @Inject
    private Executor executor;

    @Inject
    private ExecutorFacadeImpl executorFacade;

    private ExecutorServer executorServer;

    @Override
    public Order getOrder() {
        return Order.FIRST;
    }

    @Override
    public void afterPropertiesSet() {
        configureDB();
        if (props.getBoolean("metadata.enable", true)) {
            initDataBuilder();
            startConsumer();
        }
        executor.init();
        logger.debug("executor init finished");
        startExecutorRpc();
        logger.debug("executor rpc server started");

    }


    private void initDataBuilder() {
        // Verify whether the operator & task exists
        try {
            checkOperator(OperatorParam.MCE.getName(), OperatorParam.MSE.getName());
            operatorUploadService.autoUpload();

            for (OperatorParam value : OperatorParam.values()) {
                switch (value) {
                    case MCE:
                        List<Long> ids = dbOperator.fetchAll("select id from kun_mt_datasource", rs -> rs.getLong("id"));
                        List<String> taskNames = ids.stream().map(id -> "mce-task-auto:" + id).collect(Collectors.toList());
                        checkTask(props.getLong(OperatorParam.MCE.getName()), taskNames.toArray(new String[taskNames.size()]));
                        checkTask(props.getLong(OperatorParam.MCE.getName()), TaskParam.MCE_TASK.getName());
                        break;
                    case MSE:
                        checkTask(props.getLong(OperatorParam.MSE.getName()), TaskParam.MSE_TASK.getName());
                        break;
                    default:
                        throw new UnsupportedOperationException("Unsupported value: " + value);
                }
            }
        } catch (Exception e) {
            logger.error("Init DataBuilder Task error: ", e);
        }
    }

    private void startConsumer() {
        metadataConsumerStarter.start();
    }

    private Optional<Operator> findOperatorByName(String operatorName) {
        return workflowServiceFacade.getOperator(operatorName);
    }

    private Optional<Task> findTaskByName(String taskName) {
        return workflowServiceFacade.getTask(taskName);
    }

    private void createOperator(String operatorName) {
        Operator operatorOfCreated = workflowServiceFacade.saveOperator(operatorName, MetadataSysTaskBuilder.buildOperatorForCreate(operatorName));
        setProp(operatorName, operatorOfCreated.getId().toString());
    }

    private void createTask(Long operatorId, String taskName) {
        Task taskOfCreated = workflowServiceFacade.createTask(MetadataSysTaskBuilder.buildTaskForCreate(taskName,
                operatorId, props));
        setProp(taskName, taskOfCreated.getId().toString());
    }

    private void checkOperator(String... operatorNames) {
        for (String operatorName : operatorNames) {
            Optional<Operator> operatorOpt = findOperatorByName(operatorName);
            if (operatorOpt.isPresent()) {
                props.put(operatorName, operatorOpt.get().getId().toString());
            } else {
                createOperator(operatorName);
                logger.info("Create Operator: {} Success", operatorName);
            }
        }
    }

    private void checkTask(Long operatorId, String... taskNames) {
        for (String taskName : taskNames) {
            Optional<Task> taskOpt = findTaskByName(taskName);
            if (taskOpt.isPresent()) {
                props.put(taskName, taskOpt.get().getId().toString());
            } else {
                createTask(operatorId, taskName);
                logger.info("Create Task: {} Success", taskName);
            }
        }
    }

    private void setProp(String key, String value) {
        props.put(key, value);
    }

    private void configureDB() {
        DatabaseSetup setup = new DatabaseSetup(dataSource, props);
        setup.start();
    }

    private void startExecutorRpc() {
        //start rpc server
        try {
            logger.debug("going to start rpc server...");
            Integer rpcPort = props.getInt("rpc.port");
            ServerBuilder serverBuilder = ServerBuilder.forPort(rpcPort)
                    .addService(executorFacade);
            executorServer = new ExecutorServer(serverBuilder, rpcPort);
            executorServer.start();
        } catch (Throwable e) {
            logger.error("start executor rpc server failed", e);
        }
    }
}
