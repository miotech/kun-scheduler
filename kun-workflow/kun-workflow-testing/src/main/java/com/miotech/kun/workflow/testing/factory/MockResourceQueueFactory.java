package com.miotech.kun.workflow.testing.factory;

import com.miotech.kun.workflow.core.model.resource.ResourceQueue;

public class MockResourceQueueFactory {

    public static ResourceQueue createResourceQueue() {
        return new ResourceQueue("default", null, null, 10);
    }
}
