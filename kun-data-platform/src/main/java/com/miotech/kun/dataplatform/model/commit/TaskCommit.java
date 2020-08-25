package com.miotech.kun.dataplatform.model.commit;

import java.time.OffsetDateTime;

public class TaskCommit {

    private final Long id;

    private final Long definitionId;

    private final String version;

    private final String message;

    private final TaskSnapshot snapshot;

    private final Long committer;

    private final OffsetDateTime committedAt;

    private final CommitType commitType;

    private final CommitStatus commitStatus;

    private final boolean latestCommit;

    public TaskCommit(Long id, Long definitionId, String version, String message, TaskSnapshot snapshot, Long committer, OffsetDateTime committedAt, CommitType commitType, CommitStatus commitStatus, boolean latestCommit) {
        this.id = id;
        this.definitionId = definitionId;
        this.version = version;
        this.message = message;
        this.snapshot = snapshot;
        this.committer = committer;
        this.committedAt = committedAt;
        this.commitType = commitType;
        this.commitStatus = commitStatus;
        this.latestCommit = latestCommit;
    }

    public Long getId() {
        return id;
    }

    public Long getDefinitionId() {
        return definitionId;
    }

    public String getVersion() {
        return version;
    }

    public String getMessage() {
        return message;
    }

    public TaskSnapshot getSnapshot() {
        return snapshot;
    }

    public Long getCommitter() {
        return committer;
    }

    public OffsetDateTime getCommittedAt() {
        return committedAt;
    }

    public CommitType getCommitType() {
        return commitType;
    }

    public CommitStatus getCommitStatus() {
        return commitStatus;
    }

    public boolean isLatestCommit() {
        return latestCommit;
    }

    public Builder cloneBuilder() {
        return new Builder()
                .withId(id)
                .withDefinitionId(definitionId)
                .withVersion(version)
                .withMessage(message)
                .withSnapshot(snapshot)
                .withCommitter(committer)
                .withCommittedAt(committedAt)
                .withCommitType(commitType)
                .withCommitStatus(commitStatus)
                .withLatestCommit(latestCommit);
    };

    public static Builder newBuilder() { return new Builder(); }

    public static final class Builder {
        private Long id;
        private Long definitionId;
        private String version;
        private String message;
        private TaskSnapshot snapshot;
        private Long committer;
        private OffsetDateTime committedAt;
        private CommitType commitType;
        private CommitStatus commitStatus;
        private boolean latestCommit;

        private Builder() {
        }

        public static Builder aTaskCommit() {
            return new Builder();
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withDefinitionId(Long definitionId) {
            this.definitionId = definitionId;
            return this;
        }

        public Builder withVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withSnapshot(TaskSnapshot snapshot) {
            this.snapshot = snapshot;
            return this;
        }

        public Builder withCommitter(Long committer) {
            this.committer = committer;
            return this;
        }

        public Builder withCommittedAt(OffsetDateTime committedAt) {
            this.committedAt = committedAt;
            return this;
        }

        public Builder withCommitType(CommitType commitType) {
            this.commitType = commitType;
            return this;
        }

        public Builder withCommitStatus(CommitStatus commitStatus) {
            this.commitStatus = commitStatus;
            return this;
        }

        public Builder withLatestCommit(boolean latestCommit) {
            this.latestCommit = latestCommit;
            return this;
        }

        public TaskCommit build() {
            return new TaskCommit(id, definitionId, version, message, snapshot, committer, committedAt, commitType, commitStatus, latestCommit);
        }
    }
}
