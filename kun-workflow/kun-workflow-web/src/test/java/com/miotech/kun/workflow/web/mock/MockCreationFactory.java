package com.miotech.kun.workflow.web.mock;


public class MockCreationFactory {

    public static MockCreation createMockObject() {
        return createMockObject(1, "test");
    }

    public static MockCreation createMockObject(long id, String name) {
        return MockCreation.newBuilder()
                .withId(id)
                .withName(name)
                .build();
    }
}
