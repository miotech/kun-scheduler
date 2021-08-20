package com.miotech.kun.workflow.core.model.task;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import javax.annotation.Nullable;
import java.time.ZoneOffset;
import java.util.Objects;

@JsonDeserialize(builder = ScheduleConf.ScheduleConfBuilder.class)
public class ScheduleConf {
    private final ScheduleType type;

    @Nullable
    private final String cronExpr;

    private final String timeZone;


    public ScheduleConf(ScheduleType type, String cronExpr) {
        this(type, cronExpr, null);
    }

    @JsonCreator
    public ScheduleConf(@JsonProperty("type") ScheduleType type,
                        @JsonProperty("cronExpr") @Nullable String cronExpr,
                        @JsonProperty("timeZone") @Nullable String timeZone) {
        this.type = type;
        this.cronExpr = cronExpr;
        if (!type.equals(ScheduleType.NONE) && Objects.isNull(timeZone)) {
            this.timeZone = ZoneOffset.UTC.getId();
        } else {
            this.timeZone = timeZone;
        }
    }

    public ScheduleType getType() {
        return type;
    }


    @Nullable
    public String getCronExpr() {
        return cronExpr;
    }

    @Nullable
    public String getTimeZone() {
        return timeZone;
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

    public static ScheduleConfBuilder newBuilder() {
        return new ScheduleConfBuilder();
    }

    @JsonPOJOBuilder
    public static final class ScheduleConfBuilder {
        private ScheduleType type;
        private String cronExpr;
        private String timeZone;

        private ScheduleConfBuilder() {
        }

        public ScheduleConfBuilder withType(ScheduleType type) {
            this.type = type;
            return this;
        }

        public ScheduleConfBuilder withCronExpr(String cronExpr) {
            this.cronExpr = cronExpr;
            return this;
        }

        public ScheduleConfBuilder withTimeZone(String timeZone) {
            this.timeZone = timeZone;
            return this;
        }

        public ScheduleConf build() {
            return new ScheduleConf(type, cronExpr, timeZone);
        }
    }
}
