package com.miotech.kun.workflow.core.model.task;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import javax.annotation.Nullable;
import java.util.Objects;

@JsonDeserialize(builder = ScheduleConf.ScheduleConfBuilder.class)
public class ScheduleConf {
    private final ScheduleType type;

    @Nullable
    private final String cronExpr;

    @JsonCreator
    public ScheduleConf(@JsonProperty("type") ScheduleType type,
                        @JsonProperty("cronExpr") @Nullable String cronExpr) {
        this.type = type;
        this.cronExpr = cronExpr;
    }

    public ScheduleType getType() {
        return type;
    }

    @Nullable
    public String getCronExpr() {
        return cronExpr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleConf that = (ScheduleConf) o;
        return type == that.type &&
                Objects.equals(cronExpr, that.cronExpr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, cronExpr);
    }

    @JsonPOJOBuilder
    public static final class ScheduleConfBuilder {
        private ScheduleType type;
        private String cronExpr;

        private ScheduleConfBuilder() {
        }

        public static ScheduleConfBuilder aScheduleConf() {
            return new ScheduleConfBuilder();
        }

        public ScheduleConfBuilder withType(ScheduleType type) {
            this.type = type;
            return this;
        }

        public ScheduleConfBuilder withCronExpr(String cronExpr) {
            this.cronExpr = cronExpr;
            return this;
        }

        public ScheduleConf build() {
            return new ScheduleConf(type, cronExpr);
        }
    }
}
