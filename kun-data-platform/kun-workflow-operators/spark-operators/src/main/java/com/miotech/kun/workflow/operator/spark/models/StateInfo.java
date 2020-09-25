package com.miotech.kun.workflow.operator.spark.models;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StateInfo {

    public enum State {
        NOT_STARTED("not_started"),
        STARTING("starting"),
        IDLE("idle"),
        BUSY("busy"),
        RUNNING("running"),
        SHUTTING_DOWN("shutting_down"),
        ERROR("error"),
        DEAD("dead"),
        KILLED("killed"),
        SUCCESS("success")
        ;

        private String name;
        private static final Map<String, State> namingMap = new HashMap<>();
        static {
            for (State model : State.values()) {
                namingMap.put(model.getName(), model);
            }
        }

        State(String name) {
            this.name = name;
        }

        public String getName() { return this.name; }

        @JsonCreator
        public static State fromString(String string) {
            return Optional
                    .ofNullable(namingMap.get(string))
                    .orElseThrow(() -> new IllegalArgumentException(string));
        }

        public boolean isAvailable() {
            return this.equals(IDLE) || this.equals(BUSY) || this.equals(RUNNING);
        }

        public boolean isFinished() {
            return this.equals(SHUTTING_DOWN)
                    || this.equals(ERROR)
                    || this.equals(SUCCESS)
                    || this.equals(KILLED)
                    || this.equals(DEAD);
        }
    }

    private int id;

    private State state;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
