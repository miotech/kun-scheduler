package com.miotech.kun.workflow.executor;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.model.WorkerLogs;
import com.miotech.kun.workflow.core.model.executor.ExecutorInfo;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.executor.config.DispatchExecutorConfig;
import com.miotech.kun.workflow.executor.config.ExecutorConfig;
import com.miotech.kun.workflow.executor.kubernetes.KubeExecutorConfig;
import com.miotech.kun.workflow.executor.kubernetes.KubernetesExecutor;
import com.miotech.kun.workflow.executor.local.LocalExecutor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class DispatchExecutor implements Executor{

    private final Logger logger = LoggerFactory.getLogger(DispatchExecutor.class);

    private static Map<String, Executor> executorManager;

    private static Map<String, String> taskLabelToExecutorNameMap;

    private String defaultExecutor;

    private final DispatchExecutorConfig dispatchExecutorConfig;

    @Inject
    private TaskRunDao taskRunDao;

    public DispatchExecutor(DispatchExecutorConfig dispatchExecutorConfig) {
        this.dispatchExecutorConfig = dispatchExecutorConfig;
        logger.info("initiating dispatch executor...");
        defaultExecutor = dispatchExecutorConfig.getDefaultExecutor();
        taskLabelToExecutorNameMap = new HashMap<>();
        executorManager = new HashMap<>();
        List<ExecutorConfig> executorConfigList = dispatchExecutorConfig.getExecutorConfigList();
        for (ExecutorConfig executorConfig : executorConfigList) {
            String executorName = executorConfig.getName();
            ExecutorKind executorKind = ExecutorKind.valueOf(executorConfig.getKind().toUpperCase());
            Executor executor;
            if(executorKind.equals(ExecutorKind.LOCAL)){
                executor = new LocalExecutor(executorConfig);
                taskLabelToExecutorNameMap.put("LOCAL",executorName);
            } else if(executorKind.equals(ExecutorKind.KUBERNETES)){
                KubeExecutorConfig kubeExecutorConfig = (KubeExecutorConfig) executorConfig;
                executor = new KubernetesExecutor(kubeExecutorConfig,executorName);
                String[] labels = StringUtils.split(kubeExecutorConfig.getLabel(), ",");
                for (String label : labels) {
                    taskLabelToExecutorNameMap.put(label, executorName);
                }
            } else {
                throw new IllegalArgumentException("executor kind " + executorKind + " not support");
            }
            logger.info(executorName + " executor create success");
            executorManager.put(executorName, executor);

        }

    }


    public void init() {
        for (Map.Entry<String, Executor> entry : executorManager.entrySet()) {
            logger.info("init {} executor", entry.getKey());
            Executor executor = entry.getValue();
            executor.init();
        }
    }


    @Override
    public boolean submit(TaskAttempt taskAttempt) {
        Executor executor = findExecutor(taskAttempt);
        String runtimeLabel = defaultExecutor;
        if(taskAttempt.getExecutorLabel() != null){
            runtimeLabel = taskAttempt.getExecutorLabel();
        }
        TaskAttempt attemptWithLabel = taskAttempt.cloneBuilder()
                .withRuntimeLabel(runtimeLabel)
                .build();
        taskRunDao.updateAttempt(attemptWithLabel);
        return executor.submit(attemptWithLabel);
    }

    @Override
    public boolean cancel(Long taskAttemptId) {
        Executor executor = findExecutor(taskAttemptId);
        return executor.cancel(taskAttemptId);
    }

    @Override
    public void shutdown() {
        //do nothing
    }

    @Override
    public void injectMembers(Injector injector) {
        for (Map.Entry<String, Executor> entry : executorManager.entrySet()) {
            logger.info("inject {} executor", entry.getKey());
            Executor executor = entry.getValue();
            executor.injectMembers(injector);
        }
        injector.injectMembers(this);
    }

    @Override
    public boolean reset() {
        for (Map.Entry<String, Executor> entry : executorManager.entrySet()) {
            logger.info("reset {} executor", entry.getKey());
            Executor executor = entry.getValue();
            executor.reset();
        }
        return true;

    }

    @Override
    public boolean recover() {
        logger.info("dispatch executor recovering. current executor manager size {}", executorManager.size());
        for (Map.Entry<String, Executor> entry : executorManager.entrySet()) {
            logger.info("recover {} executor", entry.getKey());
            Executor executor = entry.getValue();
            executor.recover();
        }
        return true;
    }

    @Override
    public WorkerLogs workerLog(Long taskAttemptId, Integer startLine, Integer endLine) {
        Executor executor = findExecutor(taskAttemptId);
        return executor.workerLog(taskAttemptId, startLine, endLine);
    }

    @Override
    public void uploadOperator(Long operatorId, String localFile) {
        for (Map.Entry<String, Executor> entry : executorManager.entrySet()) {
            logger.info("upload operator {} to executor {}", operatorId, entry.getKey());
            Executor executor = entry.getValue();
            executor.uploadOperator(operatorId, localFile);
        }
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

    @Override
    public boolean getMaintenanceMode() {
        HashSet<Boolean> set = new HashSet<>();
        for (Map.Entry<String, Executor> entry : executorManager.entrySet()) {
            boolean maintenanceMode = entry.getValue().getMaintenanceMode();
            set.add(maintenanceMode);
        }
        return set.contains(true);
    }

    @Override
    public void setMaintenanceMode(boolean mode) {
        for (Map.Entry<String, Executor> entry : executorManager.entrySet()) {
            Executor executor = entry.getValue();
            executor.setMaintenanceMode(mode);
        }
    }

    @Override
    public ExecutorInfo getExecutorInfo() {
        Map<String, Object> extraInfo = dispatchExecutorConfig.getExecutorConfigList().stream()
                .collect(Collectors.toMap(ExecutorConfig::getName, executorConfig -> ExecutorInfo.newBuilder()
                        .withKind(executorConfig.getKind())
                        .withName(executorConfig.getName())
                        .withLabels(Arrays.asList(StringUtils.split(executorConfig.getLabel(), ",")))
                        .withResourceQueues(executorConfig.getResourceQueues())
                        .build()));
        return ExecutorInfo.newBuilder()
                .withKind(dispatchExecutorConfig.getKind())
                .withExtraInfo(extraInfo)
                .build();
    }

    private Executor findExecutor(Long taskAttemptId) {
        TaskAttempt taskAttempt = taskRunDao.fetchAttemptById(taskAttemptId).get();
        return findExecutor(taskAttempt);
    }

    private Executor findExecutor(TaskAttempt taskAttempt) {
        String executorLabel = taskAttempt.getExecutorLabel();
        String executorName;
        if (executorLabel == null || executorLabel.isEmpty() || taskLabelToExecutorNameMap.get(executorLabel) == null) {
            executorName = defaultExecutor;
        } else {
            executorName = taskLabelToExecutorNameMap.get(executorLabel);
        }
        return executorManager.get(executorName);
    }

}
