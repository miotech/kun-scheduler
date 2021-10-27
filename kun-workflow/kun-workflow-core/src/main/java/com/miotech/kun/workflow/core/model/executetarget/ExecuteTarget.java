package com.miotech.kun.workflow.core.model.executetarget;

import java.util.Map;
import java.util.Objects;

public class ExecuteTarget{
    private final Long id;
    private final String name;
    private final Map<String,Object> config;

    public ExecuteTarget(Long id, String name, Map<String, Object> config) {
        this.id = id;
        this.name = name;
        this.config = config;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public static ExecuteTargetBuilder newBuilder(){
        return new ExecuteTargetBuilder();
    }

    public ExecuteTargetBuilder cloneBuilder(){
        return ExecuteTarget.newBuilder()
                .withId(id)
                .withName(name)
                .withConfig(config);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExecuteTarget)) return false;
        ExecuteTarget that = (ExecuteTarget) o;
        return getId().equals(that.getId()) &&
                getName().equals(that.getName()) &&
                Objects.equals(getConfig(), that.getConfig());
    }

    @Override
    public String toString() {
        return "ExecuteTarget{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", config=" + config +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getConfig());
    }

    public static final class ExecuteTargetBuilder {
        private Long id;
        private String name;
        private Map<String,Object> config;

        private ExecuteTargetBuilder() {
        }

        public ExecuteTargetBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public ExecuteTargetBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ExecuteTargetBuilder withConfig(Map<String, Object> config) {
            this.config = config;
            return this;
        }

        public ExecuteTarget build() {
            return new ExecuteTarget(id, name, config);
        }
    }
}
