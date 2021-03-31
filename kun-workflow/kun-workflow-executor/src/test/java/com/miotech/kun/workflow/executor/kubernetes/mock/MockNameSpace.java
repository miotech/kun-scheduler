package com.miotech.kun.workflow.executor.kubernetes.mock;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Watch;

import java.util.ArrayList;
import java.util.List;

public class MockNameSpace {

    private String name;
    private List<Pod> items = new ArrayList<>();
    private List<Watch> watches = new ArrayList();

    public MockNameSpace(String name) {
        this.name = name;
    }

    public void createPod(Pod pod){
        items.add(pod);
    }

}
