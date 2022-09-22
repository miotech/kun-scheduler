package com.miotech.kun.metadata.core.model.connection;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class AthenaConnectionConfigInfo extends JdbcConnectionConfigInfo {
    private final String athenaUrl;

    private final String athenaUsername;

    private final String athenaPassword;

    @JsonCreator
    public AthenaConnectionConfigInfo(@JsonProperty("connectionType") ConnectionType connectionType,
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
    public boolean sameDatasource(Object o) {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AthenaConnectionConfigInfo that = (AthenaConnectionConfigInfo) o;
        return Objects.equals(athenaUrl, that.athenaUrl) && Objects.equals(athenaUsername, that.athenaUsername) && Objects.equals(athenaPassword, that.athenaPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), athenaUrl, athenaUsername, athenaPassword);
    }
}
