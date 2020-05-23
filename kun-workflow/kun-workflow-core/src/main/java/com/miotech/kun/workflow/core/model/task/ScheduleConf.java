package com.miotech.kun.workflow.core.model.task;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.annotation.Nullable;
import java.util.Objects;

@JsonDeserialize
public class ScheduleConf {
    private final ScheduleType type;

    @Nullable
    private final String cronExpr;

    public ScheduleConf(ScheduleType type, @Nullable String cronExpr) {
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
}
