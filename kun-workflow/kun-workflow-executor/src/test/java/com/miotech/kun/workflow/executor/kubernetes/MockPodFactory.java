package com.miotech.kun.workflow.executor.kubernetes;

import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import io.fabric8.kubernetes.api.model.Pod;

public class MockPodFactory {

    public static Pod create(){
        WorkflowIdGenerator.nextTaskAttemptId()
    }
}
