package com.miotech.kun.dataplatform.model.deploy;

public class DeployCommit {

    private final Long deployId;

    private final Long commit;

    private final DeployStatus deployStatus;

    public DeployCommit(Long deployId, Long commit, DeployStatus deployStatus) {
        this.deployId = deployId;
        this.commit = commit;
        this.deployStatus = deployStatus;
    }

    public static Builder newBuilder() { return new Builder(); }

    public Builder cloneBuilder() {
        return newBuilder()
                .withCommit(commit)
                .withDeployId(deployId)
                .withDeployStatus(deployStatus);
    }

    public Long getDeployId() {
        return deployId;
    }

    public Long getCommit() {
        return commit;
    }

    public DeployStatus getDeployStatus() {
        return deployStatus;
    }

    public static final class Builder {
        private Long deployId;
        private Long commit;
        private DeployStatus deployStatus;

        private Builder() {
        }

        public static Builder aDeployCommit() {
            return new Builder();
        }

        public Builder withDeployId(Long deployId) {
            this.deployId = deployId;
            return this;
        }

        public Builder withCommit(Long commit) {
            this.commit = commit;
            return this;
        }

        public Builder withDeployStatus(DeployStatus deployStatus) {
            this.deployStatus = deployStatus;
            return this;
        }

        public DeployCommit build() {
            return new DeployCommit(deployId, commit, deployStatus);
        }
    }
}
