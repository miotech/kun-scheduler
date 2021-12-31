package com.miotech.kun.workflow.core.model.worker;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class WorkerImage {

    private final Long id;

    private final String imageName;

    private final String version;

    private final Boolean active;

    private final OffsetDateTime created_at;

    private final OffsetDateTime updated_at;

    @JsonCreator
    public WorkerImage(@JsonProperty("id")  Long id,
                       @JsonProperty("imageName") String imageName,
                       @JsonProperty("version") String version,
                       @JsonProperty("active") Boolean active,
                       @JsonProperty("created_at") OffsetDateTime created_at,
                       @JsonProperty("updated_at") OffsetDateTime updated_at) {
        this.id = id;
        this.imageName = imageName;
        this.version = version;
        this.active = active;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public Long getId() {
        return id;
    }

    public String getImageName() {
        return imageName;
    }

    public String getVersion() {
        return version;
    }

    public Boolean getActive() {
        return active;
    }

    public OffsetDateTime getCreated_at() {
        return created_at;
    }

    public OffsetDateTime getUpdated_at() {
        return updated_at;
    }

    public static WorkerImageBuilder newBuilder(){
        return new WorkerImageBuilder();
    }

    public WorkerImageBuilder cloneBuilder(){
        return newBuilder()
                .withId(id)
                .withImageName(imageName)
                .withVersion(version)
                .withActive(active)
                .withCreated_at(created_at)
                .withUpdated_at(updated_at);
    }


    public static final class WorkerImageBuilder {
        private Long id;
        private String imageName;
        private String version;
        private Boolean active;
        private OffsetDateTime created_at;
        private OffsetDateTime updated_at;

        private WorkerImageBuilder() {
        }


        public WorkerImageBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public WorkerImageBuilder withImageName(String imageName) {
            this.imageName = imageName;
            return this;
        }


        public WorkerImageBuilder withVersion(String version) {
            this.version = version;
            return this;
        }

        public WorkerImageBuilder withActive(Boolean active) {
            this.active = active;
            return this;
        }

        public WorkerImageBuilder withCreated_at(OffsetDateTime created_at) {
            this.created_at = created_at;
            return this;
        }

        public WorkerImageBuilder withUpdated_at(OffsetDateTime updated_at) {
            this.updated_at = updated_at;
            return this;
        }

        public WorkerImage build() {
            return new WorkerImage(id, imageName, version, active, created_at, updated_at);
        }
    }

}
