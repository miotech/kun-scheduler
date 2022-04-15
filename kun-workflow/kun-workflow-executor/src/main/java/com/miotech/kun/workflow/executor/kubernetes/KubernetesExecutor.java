package com.miotech.kun.workflow.executor.kubernetes;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.miotech.kun.commons.utils.InitializingBean;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.StorageManager;
import com.miotech.kun.workflow.core.model.WorkerLogs;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.executor.storage.StorageManagerFactory;
import com.miotech.kun.workflow.executor.storage.StorageType;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KubernetesExecutor implements Executor, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(KubernetesExecutor.class);
    private final PodLifeCycleManager podLifeCycleManager;//pod生命周期管理
    private final KubernetesResourceManager kubernetesResourceManager;//管理kubernetes资源配额
    private final StorageManager storageManager;
    private final String name;
    private final KubeExecutorConfig kubeExecutorConfig;

    @Inject
    public KubernetesExecutor(PodLifeCycleManagerFactory podLifeCycleManagerFactory,
                              KubernetesResourceManagerFactory kubernetesResourceManagerFactory,
                              PodEventMonitorFactory podEventMonitorFactory,
                              StorageManagerFactory storageManagerFactory,
                              @Assisted KubeExecutorConfig kubeExecutorConfig,
                              @Assisted String name) {
        if (podEventMonitorFactory == null) {
            logger.warn("podEventMonitorFactory is not injected");
        }
        if (podLifeCycleManagerFactory == null) {
            logger.warn("podLifeCycleManagerFactory is not injected");
        }
        KubernetesClient kubernetesClient = new DefaultKubernetesClient(kubeExecutorConfig.getK8sClientConfig());
        KubernetesResourceManager kubernetesResourceManager = kubernetesResourceManagerFactory.create(kubernetesClient, name);
        StorageType storageType =  StorageType.valueOf(kubeExecutorConfig.getStorageConfig().get("type").toUpperCase());
        StorageManager storageManager = storageManagerFactory.createStorageManager(storageType);
        this.kubeExecutorConfig = kubeExecutorConfig;
        this.podLifeCycleManager = podLifeCycleManagerFactory.create(kubernetesClient, kubernetesResourceManager,
                podEventMonitorFactory.create(kubernetesClient, name), name , storageManager);
        this.kubernetesResourceManager = kubernetesResourceManager;
        this.name = name;
        this.storageManager = storageManager;
        logger.info("k8s executor: {}, initialize", name);
    }


    public boolean submit(TaskAttempt taskAttempt) {
        logger.info("submit taskAttemptId = {} to executor", taskAttempt.getId());
        logger.info("k8s executor : {}, submit", name);
        podLifeCycleManager.start(taskAttempt);
        return true;
    }


    @Override
    public boolean reset() {
        logger.info("kubernetes going to reset");
        podLifeCycleManager.reset();
        return true;
    }

    @Override
    public boolean recover() {
        logger.info("kubernetes executor: {} going to recover", name);
        podLifeCycleManager.recover();
        return true;
    }

    @Override
    public WorkerLogs workerLog(Long taskAttemptId, Integer startLine, Integer endLine) {
        logger.info("k8s executor: {} worker log", name);
        try {
            Integer tailLines = startLine == 0 ? Integer.MAX_VALUE : -startLine;
            String logs = podLifeCycleManager.getLog(taskAttemptId, tailLines);
            List<String> logList = coverLogsToList(logs);
            Integer lineCount = logList.size();
            logger.debug("get logs from running worker success,line count = {}", lineCount);
            return new WorkerLogs(logList,startLine,endLine,lineCount);
        }catch (Exception e){

        }
        return storageManager.workerLog(taskAttemptId,startLine,endLine);
    }

    @Override
    public void uploadOperator(Long operatorId, String localFile) {
        storageManager.uploadOperator(operatorId,localFile);
    }

    @Override
    public boolean cancel(Long taskAttemptId) {
        podLifeCycleManager.stop(taskAttemptId);
        return true;
    }

    @Override
    public void changePriority(long taskAttemptId, String queueName, Integer priority) {
        kubernetesResourceManager.changePriority(taskAttemptId, queueName, priority);
    }

    @Override
    public ResourceQueue createResourceQueue(ResourceQueue resourceQueue) {
        return kubernetesResourceManager.createResourceQueue(resourceQueue);
    }

    public ResourceQueue updateResourceQueue(ResourceQueue resourceQueue) {
        return kubernetesResourceManager.updateResourceQueue(resourceQueue);
    }

    @Override
    public void afterPropertiesSet() {
        podLifeCycleManager.run();
        Map<String,String> storageConfig = kubeExecutorConfig.getStorageConfig();
        storageManager.init(storageConfig);
    }

    private List<String> coverLogsToList(String logs) {
        return Arrays.stream(logs.split("\n")).collect(Collectors.toList());
    }
}
