package com.miotech.kun.workflow.executor.kubernetes;

import com.miotech.kun.workflow.core.model.common.WorkerSnapshot;
import com.miotech.kun.workflow.executor.EventHandler;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.*;

import java.util.Map;

public class PodEventMonitor{
    private KubernetesClient kubernetesClient;
    private Map<String, EventHandler> registerHandlers;
    public void start(){

    }
    public boolean register(WorkerSnapshot workerSnapshot, EventHandler handler){//为pod注册一个watcher监控pod的状态变更
        kubernetesClient.pods().watch(new PodStatusWatcher());
        return false;
    }
    public boolean unRegister(long taskAttemptId){
        return false;
    }
    //监控pod状态变更
    class PodStatusWatcher implements Watcher {

        @Override
        public void eventReceived(Action action, Object o) {

        }

        @Override
        public void onClose(WatcherException e) {

        }
    }

    //通过kubeClient轮询当前正在运行的pod
    class pollingPodsStatus implements Runnable{
        @Override
        public void run() {
            PodList podList = kubernetesClient.pods().withLabel("").list();
        }
    }

    public static void main(String args[]){
        Config config = new ConfigBuilder().withMasterUrl("http://localhost:9880").build();
        KubernetesClient client = new DefaultKubernetesClient(config);
        PodList podList = client.pods().list();
        Pod pod = podList.getItems().get(0);
        System.out.println(podList);
    }

}
