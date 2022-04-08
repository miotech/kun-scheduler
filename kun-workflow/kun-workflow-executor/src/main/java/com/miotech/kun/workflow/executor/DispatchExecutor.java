package com.miotech.kun.workflow.executor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.InitializingBean;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.executor.kubernetes.KubernetesExecutor;
import com.miotech.kun.workflow.executor.kubernetes.KubernetesExecutorFactory;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class DispatchExecutor implements Executor, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(DispatchExecutor.class);

    private static Map<String, Executor> executorManager;

    private static Map<String, String> taskLabelToExecutorNameMap;

    @Inject
    private KubernetesExecutorFactory kubernetesExecutorFactory;

    private String DEFAULT_EXECUTOR_NAME;

    @Inject
    private Props props;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    public DispatchExecutor(Props props) {
        this.props = props;
    }


    public void init() {
        logger.info("initiating dispatch executor...");
        DEFAULT_EXECUTOR_NAME = props.getString("executor.env.default");
        taskLabelToExecutorNameMap = new HashMap<>();
        executorManager = new HashMap<>();
        //prod read from props
        List<String> executorNames = Arrays.asList(StringUtils.split(props.getString("executor.env.executorName"), ","));
        for (String executorName : executorNames) {
            Config config = buildConfigFromProps(props, executorName);
            KubernetesClient client = new DefaultKubernetesClient(config);
            KubernetesExecutor executor = kubernetesExecutorFactory.create(client, executorName);
            logger.info(executorName + " executor create success");
            executorManager.put(executorName, executor);
            String[] labels = StringUtils.split(props.getString("executor.env."+executorName+".label"), ",");
            for (String label : labels) {
                taskLabelToExecutorNameMap.put(label, executorName);
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        init();
    }

    @Override
    public Order getOrder() {
        return Order.FIRST;
    }

    @Override
    public boolean submit(TaskAttempt taskAttempt) {
        Executor executor = findExecutor(taskAttempt);
        return executor.submit(taskAttempt);
    }

    @Override
    public boolean cancel(Long taskAttemptId) {
        Executor executor = findExecutor(taskAttemptId);
        return executor.cancel(taskAttemptId);
    }

    @Override
    public boolean reset() {
        for(Map.Entry<String, Executor> entry : executorManager.entrySet()) {
            logger.info("reset {} executor", entry.getKey());
            Executor executor = entry.getValue();
            executor.reset();
        }
        return true;

    }

    @Override
    public boolean recover() {
        logger.info("dispatch executor recovering. current executor manager size {}", executorManager.size());
        for(Map.Entry<String, Executor> entry : executorManager.entrySet()) {
            logger.info("recover {} executor", entry.getKey());
            Executor executor = entry.getValue();
            executor.recover();
        }
        return true;
    }

    @Override
    public String workerLog(Long taskAttemptId, Integer tailLines) {
        Executor executor = findExecutor(taskAttemptId);
        return executor.workerLog(taskAttemptId, tailLines);
    }

    @Override
    public void changePriority(long taskAttemptId, String queueName, Integer priority) {
        Executor executor = findExecutor(taskAttemptId);
        executor.changePriority(taskAttemptId, queueName, priority);
    }

    @Override
    public ResourceQueue createResourceQueue(ResourceQueue resourceQueue) {
        throw new UnsupportedOperationException("Executor not support create resource queue currently");
    }

    @Override
    public ResourceQueue updateResourceQueue(ResourceQueue resourceQueue) {
        throw new UnsupportedOperationException("Executor not support create resource queue currently");
    }

    private Executor findExecutor(Long taskAttemptId) {
        TaskAttempt taskAttempt = taskRunDao.fetchAttemptById(taskAttemptId).get();
        return findExecutor(taskAttempt);
    }

    private Executor findExecutor(TaskAttempt taskAttempt) {
        String executorLabel = taskAttempt.getExecutorLabel();
        String executorName;
        if (executorLabel == null || executorLabel.isEmpty() || taskLabelToExecutorNameMap.get(executorLabel) == null) {
            executorName = DEFAULT_EXECUTOR_NAME;
        } else {
            executorName = taskLabelToExecutorNameMap.get(executorLabel);
        }
        return executorManager.get(executorName);
    }

    private Config buildConfigFromProps(Props props, String executorName) {
        ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.withMasterUrl(props.getString("executor.env."+executorName+".url"));
        if (props.containsKey("executor.env."+executorName+".oauthToken")) {
            configBuilder.withOauthToken(props.getString("executor.env."+executorName+".oauthToken"));
        }
        if (props.containsKey("executor.env."+executorName+".caCertFile")) {
            configBuilder.withCaCertFile(props.getString("executor.env."+executorName+".caCertFile"));
        }
        if (props.containsKey("executor.env."+executorName+".caCert")) {
            configBuilder.withCaCertData(props.getString("executor.env."+executorName+".caCert"));
        }
        if (props.containsKey("executor.env."+executorName+".clientCert")) {
            configBuilder.withClientCertData(props.getString("executor.env."+executorName+".clientCert"));
        }
        if (props.containsKey("executor.env."+executorName+".clientKey")) {
            configBuilder.withClientKeyData(props.getString("executor.env."+executorName+".clientKey"));
        }
        return configBuilder.build();
    }
}
