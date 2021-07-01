package com.miotech.kun.workflow.executor.kubernetes;

import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.model.worker.WorkerInstanceKind;
import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodStatus;

import java.time.OffsetDateTime;

import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.KUN_TASK_ATTEMPT_ID;

public class PodStatusSnapShot extends WorkerSnapshot {
    private final PodStatus podStatus;
    private final PodSpec podSpec;
    private final ObjectMeta meta;


    public PodStatusSnapShot(WorkerInstance ins, PodStatus podStatus, PodSpec podSpec, ObjectMeta meta) {
        super(ins, OffsetDateTime.now());
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
                pod.getMetadata().getName(), pod.getMetadata().getNamespace(), WorkerInstanceKind.KUBERNETES);
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
            case "ContainerCreating":
            case "pending":
            case "running":
                taskRunStatus = TaskRunStatus.RUNNING;
                break;
            case "error":
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
