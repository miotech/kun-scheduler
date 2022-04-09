package com.miotech.kun.workflow.executor.kubernetes;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.miotech.kun.commons.utils.InitializingBean;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesExecutor implements Executor, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(KubernetesExecutor.class);
    private final PodLifeCycleManager podLifeCycleManager;//pod生命周期管理
    private final KubernetesResourceManager kubernetesResourceManager;//管理kubernetes资源配额
    private final String name;

    @Inject
    public KubernetesExecutor(PodLifeCycleManagerFactory podLifeCycleManagerFactory,
                              KubernetesResourceManagerFactory kubernetesResourceManagerFactory,
                              PodEventMonitorFactory podEventMonitorFactory,
                              @Assisted KubernetesClient kubernetesClient,
                              @Assisted String name) {
        if (podEventMonitorFactory == null) {
            logger.warn("podEventMonitorFactory is not injected");
        }
        if (podLifeCycleManagerFactory == null) {
            logger.warn("podLifeCycleManagerFactory is not injected");
        }
        KubernetesResourceManager kubernetesResourceManager = kubernetesResourceManagerFactory.create(kubernetesClient, name);
        this.podLifeCycleManager = podLifeCycleManagerFactory.create(kubernetesClient, kubernetesResourceManager,
                podEventMonitorFactory.create(kubernetesClient, name), name);
        this.kubernetesResourceManager = kubernetesResourceManager;
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
    public String workerLog(Long taskAttemptId, Integer tailLines) {
        logger.info("k8s executor: {} worker log", name);
        return podLifeCycleManager.getLog(taskAttemptId, tailLines);
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
    }
}
