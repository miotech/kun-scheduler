package com.miotech.kun.workflow.operator.spark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.miotech.kun.workflow.operator.serializer.RawValueDeserializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Statement {

    public enum State {
        WAITING("waiting"),
        RUNNING("running"),
        AVAILABLE("available"),
        ERROR("error"),
        CANCELLING("cancelling"),
        CANCELED("cancelled")
        ;

        private String name;
        private static final Map<String, State> namingMap = new HashMap<>();
        static {
            for (State model : State.values()) {
                namingMap.put(model.getName(), model);
            }
        }

        State(String state) {
            this.name = state;
        }

        public String getName() { return this.name; }

        @JsonCreator
        public static State fromString(String string) {
            return Optional
                    .ofNullable(namingMap.get(string))
                    .orElseThrow(() -> new IllegalArgumentException(string));
        }

        public boolean isSuccess() {
            return this.equals(AVAILABLE);
        }

        public boolean isFailed() {
            return this.equals(CANCELLING)
                    || this.equals(ERROR)
                    || this.equals(CANCELED);
        }
    }

    private Integer id;

    private String code;

    private State state;

    private StatementOutput output;

    private double progress;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public StatementOutput getOutput() {
        return output;
    }

    public void setOutput(StatementOutput output) {
        this.output = output;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public static class StatementOutput {

        private String status;

        @JsonProperty("execution_count")
        private int executionCount;

        @JsonDeserialize(using = RawValueDeserializer.class)
        private String data;

        private String ename;

        private String evalue;

        private List<String> traceback;

        public String getStatus() {
            return status;
        }

        public int getExecutionCount() {
            return executionCount;
        }

        public String getData() {
            return data;
        }

        public String getEname() {
            return ename;
        }

        public String getEvalue() {
            return evalue;
        }

        public List<String> getTraceback() {
            return traceback;
        }

    }
}
