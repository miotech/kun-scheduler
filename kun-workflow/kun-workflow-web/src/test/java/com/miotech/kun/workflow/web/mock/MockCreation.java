package com.miotech.kun.workflow.web.mock;


public class MockCreation {
    private long id;

    private String name;

    public MockCreation() {}

    public MockCreation(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId() {
        this.id = id;
    }

    public void setName() {
        this.name = name;
    }

    public static MockCreation.Builder newBuilder() { return new Builder(); }

    public static final class Builder {
        private long id;
        private String name;

        private Builder() {
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public MockCreation build() {
            return new MockCreation(id, name);
        }
    }
}
