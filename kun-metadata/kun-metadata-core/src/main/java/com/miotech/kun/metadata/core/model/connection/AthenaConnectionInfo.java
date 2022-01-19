package com.miotech.kun.metadata.core.model.connection;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class AthenaConnectionInfo extends JdbcConnectionInfo{
    private final String athenaUrl;

    private final String athenaUsername;

    private final String athenaPassword;

    @JsonCreator
    public AthenaConnectionInfo(@JsonProperty("connectionType") ConnectionType connectionType,
                                @JsonProperty("athenaUrl") String athenaUrl,
                                @JsonProperty("athenaUsername") String athenaUsername,
                                @JsonProperty("athenaPassword") String athenaPassword) {
        super(connectionType);
        this.athenaUrl = athenaUrl;
        this.athenaUsername = athenaUsername;
        this.athenaPassword = athenaPassword;
    }

    public String getAthenaUrl() {
        return athenaUrl;
    }

    public String getAthenaUsername() {
        return athenaUsername;
    }

    public String getAthenaPassword() {
        return athenaPassword;
    }

    @Override
    @JsonIgnore
    public String getJdbcUrl() {
        return athenaUrl;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return athenaUsername;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return athenaPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AthenaConnectionInfo that = (AthenaConnectionInfo) o;
        return athenaUrl.equals(that.athenaUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(athenaUrl);
    }
}
