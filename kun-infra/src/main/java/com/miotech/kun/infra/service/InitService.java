package com.miotech.kun.infra.service;

import com.google.inject.Inject;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.web.constant.OperatorParam;
import com.miotech.kun.metadata.web.constant.TaskParam;
import com.miotech.kun.metadata.web.kafka.MetadataConsumerStarter;
import com.miotech.kun.metadata.web.util.RequestParameterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.tools.jstat.Operator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InitService {
    private static final Logger logger = LoggerFactory.getLogger(com.miotech.kun.metadata.web.service.InitService.class);

    @Inject
    private WorkflowClient workflowClient;

    @Inject
    private Props props;

    @Inject
    private javax.sql.DataSource dataSource;

    @Inject
    private RpcPublisher rpcPublisher;

    @Inject
    private MetadataServiceFacade metadataServiceFacade;

    @Inject
    private MetadataConsumerStarter metadataConsumerStarter;

    @Inject
    private DatabaseOperator dbOperator;

    @Override
    public Order getOrder() {
        return Order.LAST;
    }

    @Override
    public void afterPropertiesSet() {
        configureDB();
        initDataBuilder();
        publishRpcServices();
        startConsumer();
    }

    private void configureDB() {
        DatabaseSetup setup = new DatabaseSetup(dataSource, props);
        setup.start();
    }

    private void initDataBuilder() {
        // Verify whether the operator & task exists
        try {
            checkOperator(OperatorParam.MCE.getName(), OperatorParam.MSE.getName());
            String workflowUrl = props.getString("workflow.url");
            OperatorUpload operatorUpload = new OperatorUpload(workflowUrl);
            operatorUpload.autoUpload();

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

    private void publishRpcServices() {
        rpcPublisher.exportService(MetadataServiceFacade.class, "1.0", metadataServiceFacade);
    }

    private void startConsumer() {
        metadataConsumerStarter.start();
    }

    private Optional<Operator> findOperatorByName(String operatorName) {
        return workflowClient.getOperator(operatorName);
    }

    private Optional<Task> findTaskByName(String taskName) {
        return workflowClient.getTask(taskName);
    }

    private void createOperator(String operatorName) {
        Operator operatorOfCreated = workflowClient.saveOperator(operatorName, RequestParameterBuilder.buildOperatorForCreate(operatorName));
        setProp(operatorName, operatorOfCreated.getId().toString());
    }

    private void createTask(Long operatorId, String taskName) {
        Task taskOfCreated = workflowClient.createTask(RequestParameterBuilder.buildTaskForCreate(taskName,
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
}
