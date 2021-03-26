package com.miotech.kun.workflow.executor.kubernetes;

import com.miotech.kun.workflow.core.model.common.WorkerInstance;
import com.miotech.kun.workflow.executor.EventHandler;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.KUN_TASK_ATTEMPT_ID;
import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.KUN_WORKFLOW;

public class PodEventMonitor {

    private final Logger logger = LoggerFactory.getLogger(PodEventMonitor.class);
    @Inject
    private KubernetesClient kubernetesClient;
    private Map<Long, EventHandler> registerHandlers = new ConcurrentHashMap<>();
    private final long POLLING_PERIOD = 30 * 1000;

    public void start() {
        ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1);
        timer.scheduleAtFixedRate(new PollingPodsStatus(), 10, POLLING_PERIOD, TimeUnit.MILLISECONDS);
    }

    public boolean register(WorkerInstance workerInstance, EventHandler handler) {//为pod注册一个watcher监控pod的状态变更
        registerHandlers.put(workerInstance.getTaskAttemptId(), handler);
        kubernetesClient.pods()
                .withLabel(KUN_TASK_ATTEMPT_ID, String.valueOf(workerInstance.getTaskAttemptId()))
                .withLabel(KUN_WORKFLOW)
                .watch(new PodStatusWatcher());
        return true;
    }

    public boolean unRegister(long taskAttemptId) {
        registerHandlers.remove(taskAttemptId);
        return true;
    }


    //监控pod状态变更
    class PodStatusWatcher implements Watcher<Pod> {

        @Override
        public void eventReceived(Action action, Pod pod) {
            Long taskAttemptId = Long.parseLong(pod.getMetadata().getLabels().get(KUN_TASK_ATTEMPT_ID));
            EventHandler eventHandler = registerHandlers.get(taskAttemptId);
            eventHandler.onReceiveSnapshot(PodStatusSnapShot.fromPod(pod));
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
            PodList podList = kubernetesClient.pods()
                    .withLabel(KUN_WORKFLOW).list();
            for (Pod pod : podList.getItems()) {
                Long taskAttemptId = Long.parseLong(pod.getMetadata().getLabels().get(KUN_TASK_ATTEMPT_ID));
                EventHandler eventHandler = registerHandlers.get(taskAttemptId);
                eventHandler.onReceivePollingSnapShot(PodStatusSnapShot.fromPod(pod));
            }
        }
    }

    public static void main(String args[]) {
        Config config = new ConfigBuilder().withMasterUrl("http://localhost:9880").build();
        KubernetesClient client = new DefaultKubernetesClient(config);
        PodList podList = client.pods().list();
        Pod pod = podList.getItems().get(0);
        System.out.println(podList);
    }

}
