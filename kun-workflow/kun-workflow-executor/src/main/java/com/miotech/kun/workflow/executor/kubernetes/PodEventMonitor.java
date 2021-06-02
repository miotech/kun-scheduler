package com.miotech.kun.workflow.executor.kubernetes;

import com.miotech.kun.commons.utils.InitializingBean;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.executor.WorkerEventHandler;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.KUN_TASK_ATTEMPT_ID;
import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.KUN_WORKFLOW;

@Singleton
public class PodEventMonitor implements WorkerMonitor, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(PodEventMonitor.class);
    private KubernetesClient kubernetesClient;
    private Map<Long, WorkerEventHandler> registerHandlers = new ConcurrentHashMap<>();
    private final long POLLING_PERIOD = 5 * 1000;
    private final Props props;


    @Inject
    public PodEventMonitor(KubernetesClient kubernetesClient, Props props) {
        this.kubernetesClient = kubernetesClient;
        this.props = props;
    }

    public void start() {
        logger.info("start pod monitor...");
        ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1);
        timer.scheduleAtFixedRate(new PollingPodsStatus(), 10, POLLING_PERIOD, TimeUnit.MILLISECONDS);
        kubernetesClient.pods()
                .inNamespace(props.getString("executor.env.namespace"))
                .withLabel(KUN_WORKFLOW)
                .watch(new PodStatusWatcher());
    }

    public boolean register(Long taskAttemptId, WorkerEventHandler handler) {//为pod注册一个watcher监控pod的状态变更
        logger.debug("register pod event handler,taskAttemptId = {}", taskAttemptId);
        registerHandlers.put(taskAttemptId, handler);
        return true;
    }

    public boolean unRegister(Long taskAttemptId) {
        logger.debug("unRegister worker,taskAttemptId = {}", taskAttemptId);
        registerHandlers.remove(taskAttemptId);
        return true;
    }

    @Override
    public void unRegisterAll() {
        registerHandlers.clear();
    }

    @Override
    public void afterPropertiesSet() {
        start();
    }


    //监控pod状态变更
    class PodStatusWatcher implements Watcher<Pod> {

        @Override
        public void eventReceived(Action action, Pod pod) {
            Long taskAttemptId = Long.parseLong(pod.getMetadata().getLabels().get(KUN_TASK_ATTEMPT_ID));
            logger.debug("receive pod event taskAttemptId = {}", taskAttemptId);
            WorkerEventHandler workerEventHandler = registerHandlers.get(taskAttemptId);
            if (workerEventHandler == null) {
                logger.warn("pod with taskAttemptId = {} count not found event handler", taskAttemptId);
                return;
            }
            workerEventHandler.onReceiveSnapshot(PodStatusSnapShot.fromPod(pod));
        }

        @Override
        public void onClose(WatcherException e) {
            logger.warn("Kubernetes client has been closed", e);
        }
    }

    //通过kubeClient轮询当前正在运行的pod
    class PollingPodsStatus implements Runnable {
        @Override
        public void run() {
            try {
                PodList podList = kubernetesClient.pods()
                        .inNamespace(props.getString("executor.env.namespace"))
                        .withLabel(KUN_WORKFLOW).list();
                logger.debug("fetch pod list from kubernetes size = {}, register size = {}", podList.getItems().size(), registerHandlers.size());
                Set<Long> registerSet = new HashSet<>(registerHandlers.keySet());
                for (Pod pod : podList.getItems()) {
                    Long taskAttemptId = Long.parseLong(pod.getMetadata().getLabels().get(KUN_TASK_ATTEMPT_ID));
                    WorkerEventHandler workerEventHandler = registerHandlers.get(taskAttemptId);
                    if (workerEventHandler == null) {
                        logger.warn("pod with taskAttemptId = {} count not found event handler", taskAttemptId);
                        continue;
                    }
                    workerEventHandler.onReceivePollingSnapShot(PodStatusSnapShot.fromPod(pod));
                    registerSet.remove(taskAttemptId);
                }
                for (Long unFoundAttempt : registerSet) {
                    logger.warn("count not found pod for register handler, taskAttemptId = {}", unFoundAttempt);
                }
            } catch (Throwable e) {
                logger.error("polling pods status from kubernetes failed", e);
            }

        }
    }

}
