package com.miotech.kun.workflow.executor.kubernetes;

import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.KUN_TASK_ATTEMPT_ID;
import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.KUN_WORKFLOW;

public class PodEventMonitor implements WorkerMonitor {

    private final Logger logger = LoggerFactory.getLogger(PodEventMonitor.class);
    private KubernetesClient kubernetesClient;
    private Map<Long, WorkerEventHandler> registerHandlers = new ConcurrentHashMap<>();
    private Map<Long, Integer> unHealthWorker = new ConcurrentHashMap<>();
    private final long POLLING_PERIOD = 30 * 1000;


    @Inject
    public PodEventMonitor(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    public void start() {
        ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1);
        timer.scheduleAtFixedRate(new PollingPodsStatus(), 10, POLLING_PERIOD, TimeUnit.MILLISECONDS);
    }

    public boolean register(WorkerInstance workerInstance, WorkerEventHandler handler) {//为pod注册一个watcher监控pod的状态变更
        registerHandlers.put(workerInstance.getTaskAttemptId(), handler);
        kubernetesClient.pods()
                .withLabel(KUN_WORKFLOW)
                .withLabel(KUN_TASK_ATTEMPT_ID, String.valueOf(workerInstance.getTaskAttemptId()))
                .watch(new PodStatusWatcher());
        return true;
    }

    public boolean unRegister(long taskAttemptId) {
        registerHandlers.remove(taskAttemptId);
        unHealthWorker.remove(taskAttemptId);
        return true;
    }


    //监控pod状态变更
    class PodStatusWatcher implements Watcher<Pod> {

        @Override
        public void eventReceived(Action action, Pod pod) {
            Long taskAttemptId = Long.parseLong(pod.getMetadata().getLabels().get(KUN_TASK_ATTEMPT_ID));
            WorkerEventHandler workerEventHandler = registerHandlers.get(taskAttemptId);
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
            PodList podList = kubernetesClient.pods()
                    .withLabel(KUN_WORKFLOW).list();
            Set<Long> expectTaskAttempt = registerHandlers.keySet();
            for (Pod pod : podList.getItems()) {
                Long taskAttemptId = Long.parseLong(pod.getMetadata().getLabels().get(KUN_TASK_ATTEMPT_ID));
                expectTaskAttempt.remove(taskAttemptId);
                unHealthWorker.remove(taskAttemptId);
                WorkerEventHandler workerEventHandler = registerHandlers.get(taskAttemptId);
                workerEventHandler.onReceivePollingSnapShot(PodStatusSnapShot.fromPod(pod));
            }
            Iterator<Map.Entry<Long, Integer>> iterator = unHealthWorker.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, Integer> entry = iterator.next();
                //todo:send unHealth message
                if (expectTaskAttempt.contains(entry.getKey())) {
                    unHealthWorker.put(entry.getKey(), (entry.getValue() + 1));
                }else {
                    iterator.remove();
                }
            }
        }
    }

}
