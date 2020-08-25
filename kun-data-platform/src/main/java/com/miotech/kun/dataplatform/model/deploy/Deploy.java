package com.miotech.kun.dataplatform.model.deploy;

import java.time.OffsetDateTime;
import java.util.List;

public class Deploy {

    private final Long id;

    private final String name;

    private final Long creator;

    private final OffsetDateTime submittedAt;

    private final Long deployer;

    private final OffsetDateTime deployedAt;

    private final DeployStatus status;

    private final List<DeployCommit> commits;

    public Deploy(Long id, String name, Long creator, OffsetDateTime submittedAt, Long deployer, OffsetDateTime deployedAt, DeployStatus status, List<DeployCommit> commits) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.submittedAt = submittedAt;
        this.deployer = deployer;
        this.deployedAt = deployedAt;
        this.status = status;
        this.commits = commits;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getCreator() {
        return creator;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public Long getDeployer() {
        return deployer;
    }

    public OffsetDateTime getDeployedAt() {
        return deployedAt;
    }

    public DeployStatus getStatus() {
        return status;
    }

    public List<DeployCommit> getCommits() {
        return commits;
    }

    public static Builder newBuilder() { return new Builder(); }

    public Builder cloneBuilder() {
        return newBuilder()
                .withId(id)
                .withName(name)
                .withCreator(creator)
                .withSubmittedAt(submittedAt)
                .withCommits(commits)
                .withDeployer(deployer)
                .withDeployedAt(deployedAt)
                .withStatus(status);
    }

    public static final class Builder {
        private Long id;
        private String name;
        private Long creator;
        private OffsetDateTime submittedAt;
        private Long deployer;
        private OffsetDateTime deployedAt;
        private DeployStatus status;
        private List<DeployCommit> commits;

        private Builder() {
        }

        public static Builder aDeploy() {
            return new Builder();
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withCreator(Long creator) {
            this.creator = creator;
            return this;
        }

        public Builder withSubmittedAt(OffsetDateTime submittedAt) {
            this.submittedAt = submittedAt;
            return this;
        }

        public Builder withDeployer(Long deployer) {
            this.deployer = deployer;
            return this;
        }

        public Builder withDeployedAt(OffsetDateTime deployedAt) {
            this.deployedAt = deployedAt;
            return this;
        }

        public Builder withStatus(DeployStatus status) {
            this.status = status;
            return this;
        }

        public Builder withCommits(List<DeployCommit> commits) {
            this.commits = commits;
            return this;
        }

        public Deploy build() {
            return new Deploy(id, name, creator, submittedAt, deployer, deployedAt, status, commits);
        }
    }
}
