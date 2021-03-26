package com.miotech.kun.workflow.executor.kubernetes;

import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerInstanceEnv;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodStatus;

import java.util.Date;

import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.KUN_TASK_ATTEMPT_ID;

public class PodStatusSnapShot extends WorkerSnapshot {
    private final PodStatus podStatus;
    private final PodSpec podSpec;
    private final ObjectMeta meta;


    public PodStatusSnapShot(WorkerInstance ins, PodStatus podStatus, PodSpec podSpec, ObjectMeta meta) {
        super(ins, new Date());
        this.podStatus = podStatus;
        this.podSpec = podSpec;
        this.meta = meta;
    }

    public PodStatus getPodStatus() {
        return podStatus;
    }

    public PodSpec getPodSpec() {
        return podSpec;
    }

    public ObjectMeta getMeta() {
        return meta;
    }

    public static PodStatusSnapShot fromPod(Pod pod) {
        WorkerInstance workerInstance = new WorkerInstance(Long.parseLong(pod.getMetadata().getLabels().get(KUN_TASK_ATTEMPT_ID)),
                pod.getMetadata().getName(), WorkerInstanceEnv.KUBERNETES);
        return new PodStatusSnapShot(workerInstance, pod.getStatus(), pod.getSpec(), pod.getMetadata());
    }

    @Override
    public TaskRunStatus getStatus() {
        TaskRunStatus taskRunStatus;
        if (podStatus == null || podStatus.getPhase() == null) {
            taskRunStatus = TaskRunStatus.FAILED;
            return taskRunStatus;
        }
        String status = podStatus.getPhase().toLowerCase();
        switch (status) {
            case "pending":
                taskRunStatus = TaskRunStatus.INITIALIZING;
                break;
            case "running":
                taskRunStatus = TaskRunStatus.RUNNING;
                break;
            case "failed":
                taskRunStatus = TaskRunStatus.FAILED;
                break;
            case "succeeded":
                taskRunStatus = TaskRunStatus.SUCCESS;
                break;
            case "terminating":
                taskRunStatus = TaskRunStatus.ABORTED;
                break;
            default:
                throw new IllegalStateException("UnExpect pod status " + status);


        }
        return taskRunStatus;
    }

}
