package com.miotech.kun.workflow.executor.kubernetes.mock;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.*;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class MockNameSpaceOperation implements NonNamespaceOperation<Pod, PodList, PodResource<Pod>> {

    private Map<String,MockNameSpace> nameSpaceMap = new ConcurrentHashMap<>();
    private String nameSpace = null;
    private List<Pod> items = new ArrayList<>();

    @Override
    public Deletable withGracePeriod(long gracePeriodSeconds) {
        return null;
    }

    @Override
    public EditReplacePatchDeletable<Pod> withPropagationPolicy(DeletionPropagation propagationPolicy) {
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
}
