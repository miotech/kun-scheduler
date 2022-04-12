package com.miotech.kun.security.model.po;

import com.miotech.kun.security.common.UserStatus;
import com.miotech.kun.security.model.bo.UserExtensionInformation;

import java.time.OffsetDateTime;

public class User {

    private final Long id;

    private final String username;

    private final String password;

    private final UserExtensionInformation externalInformation;

    private final OffsetDateTime createdAt;

    private final OffsetDateTime updatedAt;

    private final UserStatus status;

    public User(Long id, String username, String password, UserExtensionInformation externalInformation,
                OffsetDateTime createdAt, OffsetDateTime updatedAt, UserStatus status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.externalInformation = externalInformation;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UserExtensionInformation getExternalInformation() {
        return externalInformation;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public UserStatus getStatus() {
        return status;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private String username;
        private String password;
        private UserExtensionInformation externalInformation;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private UserStatus status;

        private Builder() {
        }


        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withExternalInformation(UserExtensionInformation externalInformation) {
            this.externalInformation = externalInformation;
            return this;
        }

        public Builder withCreatedAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withUpdatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder withStatus(UserStatus status) {
            this.status = status;
            return this;
        }

        public User build() {
            return new User(id, username, password, externalInformation, createdAt, updatedAt, status);
        }
    }
}
