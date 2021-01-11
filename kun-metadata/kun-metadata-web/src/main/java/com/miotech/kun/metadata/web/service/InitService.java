package com.miotech.kun.metadata.web.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.rpc.RpcPublisher;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.metadata.web.constant.OperatorParam;
import com.miotech.kun.metadata.web.constant.TaskParam;
import com.miotech.kun.metadata.web.model.vo.DataSource;
import com.miotech.kun.metadata.web.util.DataDiscoveryApi;
import com.miotech.kun.metadata.web.util.RequestParameterBuilder;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.operator.OperatorUpload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class InitService {
    private static final Logger logger = LoggerFactory.getLogger(InitService.class);

    @Inject
    private WorkflowClient workflowClient;

    @Inject
    private Props props;

    @Inject
    private RpcPublisher rpcPublisher;

    @Inject
    private MetadataServiceFacade metadataServiceFacade;

    public void publishRpcServices() {
        rpcPublisher.exportService(MetadataServiceFacade.class, "1.0", metadataServiceFacade);
    }

    public void initDataBuilder() {
        // Verify whether the operator & task exists
        try {
            String dataDiscoveryUrl = props.getString("data-discovery.baseUrl");
            if (StringUtils.isBlank(dataDiscoveryUrl)) {
                return;
            }

            checkOperator(OperatorParam.MCE.getName(), OperatorParam.MSE.getName());
            String workflowUrl = props.getString("workflow.url");
            OperatorUpload operatorUpload = new OperatorUpload(workflowUrl);
            operatorUpload.autoUpload();

            for (OperatorParam value : OperatorParam.values()) {
                switch (value) {
                    case MCE:
                        DataDiscoveryApi dataDiscoveryApi = new DataDiscoveryApi(dataDiscoveryUrl);
                        List<DataSource> dataSources = dataDiscoveryApi.searchDataSources().getResult().getDatasources();
                        List<String> taskNames = dataSources.stream().map(dataSource -> "mce-task-auto:" + dataSource.getId()).collect(Collectors.toList());
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
