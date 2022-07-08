package com.miotech.kun.workflow.executor.kubernetes;

import com.google.inject.Injector;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.StorageManager;
import com.miotech.kun.workflow.core.model.WorkerLogs;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.executor.storage.StorageManagerFactory;
import com.miotech.kun.workflow.executor.storage.StorageType;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class KubernetesExecutor implements Executor {

    private final Logger logger = LoggerFactory.getLogger(KubernetesExecutor.class);
    private PodLifeCycleManager podLifeCycleManager;//pod生命周期管理
    private final KubernetesResourceManager kubernetesResourceManager;//管理kubernetes资源配额
    private final String name;
    private final KubeExecutorConfig kubeExecutorConfig;
    private StorageManager storageManager;

    public KubernetesExecutor(KubeExecutorConfig kubeExecutorConfig,
                              String name) {
        KubeConfig kubeConfig = kubeExecutorConfig.getKubeConfig();
        KubernetesClient kubernetesClient = new DefaultKubernetesClient(getK8sConfig(kubeConfig));
        kubernetesResourceManager = new KubernetesResourceManager(kubeExecutorConfig, kubernetesClient, name);
        this.kubeExecutorConfig = kubeExecutorConfig;
        StorageType storageType = StorageType.valueOf(kubeExecutorConfig.getStorage()
                .getOrDefault("type", "LOCAL").toUpperCase());
        storageManager = StorageManagerFactory.createStorageManager(storageType);
        PodEventMonitor podEventMonitor = new PodEventMonitor(kubernetesClient, kubeConfig, name);
        podLifeCycleManager = new PodLifeCycleManager(kubeExecutorConfig, podEventMonitor, kubernetesClient, kubernetesResourceManager, name, storageManager);
        this.name = name;
        logger.info("k8s executor: {}, initialize", name);
    }


    public boolean submit(TaskAttempt taskAttempt) {
        logger.info("submit taskAttemptId = {} to executor", taskAttempt.getId());
        logger.info("k8s executor : {}, submit", name);
        podLifeCycleManager.start(taskAttempt);
        return true;
    }


    @Override
    public void shutdown() {
        podLifeCycleManager.shutdown();
    }

    @Override
    public void injectMembers(Injector injector) {
        storageManager.injectMember(injector);
        kubernetesResourceManager.injectMember(injector);
        podLifeCycleManager.injectMembers(injector);
        injector.injectMembers(this);
    }

    @Override
    public void init() {
        podLifeCycleManager.init();
        Map<String, String> storageConfig = kubeExecutorConfig.getStorage();
        storageManager.init(storageConfig);
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
            return new WorkerLogs(logList, startLine, endLine, lineCount);
        } catch (Exception e) {

        }
        return storageManager.workerLog(taskAttemptId, startLine, endLine);
    }

    @Override
    public void uploadOperator(Long operatorId, String localFile) {
        storageManager.uploadOperator(operatorId, localFile);
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

    @Override
    public ResourceQueue updateResourceQueue(ResourceQueue resourceQueue) {
        return kubernetesResourceManager.updateResourceQueue(resourceQueue);
    }

    @Override
    public boolean getMaintenanceMode() {
        return podLifeCycleManager.getMaintenanceMode();
    }

    @Override
    public void setMaintenanceMode(boolean mode) {
        podLifeCycleManager.setMaintenanceMode(mode);
    }

    private List<String> coverLogsToList(String logs) {
        return Arrays.stream(logs.split("\n")).collect(Collectors.toList());
    }

    private Config getK8sConfig(KubeConfig kubeConfig) {
        String masterUrl = kubeConfig.getUrl();
        String oauthToken = kubeConfig.getOauthToken();
        String caCertFile = kubeConfig.getCaCertFile();
        String clientCert = kubeConfig.getClientCert();
        String clientKey = kubeConfig.getClientKey();
        String caCert = kubeConfig.getCaCert();

        logger.info("kubeConfig : {}", kubeConfig);

        if (isNotEmpty(masterUrl) && isNotEmpty(oauthToken) && isNotEmpty(caCertFile)) {
            logger.info("Interact with k8s cluster using oauth authentication.");
            return new ConfigBuilder()
                    .withMasterUrl(masterUrl)
                    .withOauthToken(oauthToken)
                    .withCaCertFile(caCertFile)
                    .build();
        } else if (isNotEmpty(clientCert) && isNotEmpty(clientKey) && isNotEmpty(caCert)) {
            logger.info("Interact with k8s cluster using client");
            return new ConfigBuilder()
                    .withMasterUrl(masterUrl)
                    .withOauthToken(oauthToken)
                    .withClientKeyData(clientKey)
                    .withCaCertData(caCert)
                    .withClientCertData(clientCert)
                    .build();
        } else {
            logger.info("Interact with k8s cluster using rbac authentication.");
            return Config.autoConfigure(null);
        }
    }
}
