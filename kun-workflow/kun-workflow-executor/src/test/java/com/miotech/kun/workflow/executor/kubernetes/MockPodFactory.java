package com.miotech.kun.workflow.executor.kubernetes;

import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.*;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.KUN_TASK_ATTEMPT_ID;
import static com.miotech.kun.workflow.executor.kubernetes.KubernetesConstants.KUN_WORKFLOW;

public class MockPodFactory {

    public static Pod create() {
        long taskRunId = WorkflowIdGenerator.nextTaskRunId();
        long taskAttemptId = WorkflowIdGenerator.nextTaskAttemptId(taskRunId, 1);
        return create(taskAttemptId);
    }

    public static Pod create(long taskAttemptId) {
        return create(taskAttemptId, "Pending");
    }

    public static Pod create(long taskAttemptId, String nameSpace) {
        return create(taskAttemptId, nameSpace, null);
    }

    public static Pod create(long taskAttemptId, String nameSpace, String status) {
        String workerId = KUN_WORKFLOW + taskAttemptId;
        ObjectMeta meta = new ObjectMeta();
        Map<String, String> labels = new HashMap<>();
        labels.put(KUN_TASK_ATTEMPT_ID, String.valueOf(taskAttemptId));
        labels.put(KUN_WORKFLOW, null);
        meta.setLabels(labels);
        meta.setName(workerId);
        meta.setNamespace(nameSpace);
        Pod pod = new Pod();
        pod.setMetadata(meta);
        PodStatus podStatus = new PodStatus();
        podStatus.setPhase(status);
        pod.setStatus(podStatus);
        return pod;
    }

    public static MixedOperation<Pod, PodList, PodResource<Pod>> mockMixedOperation() {
        MixedOperation<Pod, PodList, PodResource<Pod>> mixedOperation = new MixedOperation<Pod, PodList, PodResource<Pod>>() {


            @Override
            public Deletable withGracePeriod(long gracePeriodSeconds) {
                return null;
            }

            @Override
            public EditReplacePatchDeletable<Pod> withPropagationPolicy(DeletionPropagation propagationPolicy) {
                return null;
            }

            @Override
            public FilterWatchListMultiDeletable<Pod, PodList> inAnyNamespace() {
                return null;
            }

            @Override
            public Pod createOrReplace(Pod... item) {
                return null;
            }

            @Override
            public Pod create(Pod... item) {
                return null;
            }

            @Override
            public Pod create(Pod item) {
                return null;
            }

            @Override
            public Boolean delete() {
                return null;
            }

            @Override
            public FilterWatchListDeletable<Pod, PodList> withLabels(Map<String, String> labels) {
                return null;
            }

            @Override
            public FilterWatchListDeletable<Pod, PodList> withoutLabels(Map<String, String> labels) {
                return null;
            }

            @Override
            public FilterWatchListDeletable<Pod, PodList> withLabelIn(String key, String... values) {
                return null;
            }

            @Override
            public FilterWatchListDeletable<Pod, PodList> withLabelNotIn(String key, String... values) {
                return null;
            }

            @Override
            public FilterWatchListDeletable<Pod, PodList> withLabel(String key, String value) {
                return null;
            }

            @Override
            public FilterWatchListDeletable<Pod, PodList> withLabel(String key) {
                return null;
            }

            @Override
            public FilterWatchListDeletable<Pod, PodList> withoutLabel(String key, String value) {
                return null;
            }

            @Override
            public FilterWatchListDeletable<Pod, PodList> withoutLabel(String key) {
                return null;
            }

            @Override
            public FilterWatchListDeletable<Pod, PodList> withFields(Map<String, String> labels) {
                return null;
            }

            @Override
            public FilterWatchListDeletable<Pod, PodList> withField(String key, String value) {
                return null;
            }

            @Override
            public FilterWatchListDeletable<Pod, PodList> withoutFields(Map<String, String> fields) {
                return null;
            }

            @Override
            public FilterWatchListDeletable<Pod, PodList> withoutField(String key, String value) {
                return null;
            }

            @Override
            public FilterWatchListDeletable<Pod, PodList> withLabelSelector(LabelSelector selector) {
                return null;
            }

            @Override
            public FilterWatchListDeletable<Pod, PodList> withInvolvedObject(ObjectReference objectReference) {
                return null;
            }

            @Override
            public PodList list() {
                return null;
            }

            @Override
            public PodList list(Integer limitVal, String continueVal) {
                return null;
            }

            @Override
            public PodList list(ListOptions listOptions) {
                return null;
            }

            @Override
            public PodResource<Pod> load(InputStream is) {
                return null;
            }

            @Override
            public PodResource<Pod> load(URL url) {
                return null;
            }

            @Override
            public PodResource<Pod> load(File file) {
                return null;
            }

            @Override
            public PodResource<Pod> load(String path) {
                return null;
            }

            @Override
            public Boolean delete(Pod... items) {
                return null;
            }

            @Override
            public Boolean delete(List<Pod> items) {
                return null;
            }

            @Override
            public PodResource<Pod> withName(String name) {
                return null;
            }

            @Override
            public NonNamespaceOperation<Pod, PodList, PodResource<Pod>> inNamespace(String name) {
                return null;
            }

            @Override
            public Pod updateStatus(Pod item) {
                return null;
            }

            @Override
            public WatchAndWaitable<Pod> withResourceVersion(String resourceVersion) {
                return null;
            }

            @Override
            public Pod waitUntilReady(long amount, TimeUnit timeUnit) throws InterruptedException {
                return null;
            }

            @Override
            public Pod waitUntilCondition(Predicate<Pod> condition, long amount, TimeUnit timeUnit) throws InterruptedException {
                return null;
            }

            @Override
            public Waitable<Pod, Pod> withWaitRetryBackoff(long initialBackoff, TimeUnit backoffUnit, double backoffMultiplier) {
                return null;
            }

            @Override
            public Watch watch(Watcher<Pod> watcher) {
                return null;
            }

            @Override
            public Watch watch(ListOptions options, Watcher<Pod> watcher) {
                return null;
            }

            @Override
            public Watch watch(String resourceVersion, Watcher<Pod> watcher) {
                return null;
            }
        };
        return mixedOperation;
    }
}
