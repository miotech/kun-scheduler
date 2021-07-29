package com.miotech.kun.workflow.operator.spark.models;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class YarnStateInfo {

    public enum State{
        UNDEFINED("UNDEFINED"),
        SUCCEEDED("SUCCEEDED"),
        FAILED("FAILED"),
        KILLED("KILLED");


        private String name;

        private static final Map<String, LivyStateInfo.State> namingMap = new HashMap<>();
        static {
            for (LivyStateInfo.State model : LivyStateInfo.State.values()) {
                namingMap.put(model.getName(), model);
            }
        }

        State(String name) {
            this.name = name;
        }

        public String getName() { return this.name; }

        @JsonCreator
        public static LivyStateInfo.State fromString(String string) {
            return Optional
                    .ofNullable(namingMap.get(string))
                    .orElseThrow(() -> new IllegalArgumentException(string));
        }

        public boolean isFinished() {
            return this.equals(SUCCEEDED)
                    || this.equals(FAILED)
                    || this.equals(KILLED);
        }

        public boolean isSuccess(){
            return this.equals(SUCCEEDED);
        }

    }

    private LivyStateInfo.State state;

    public LivyStateInfo.State getState() {
        return state;
    }

    public void setState(LivyStateInfo.State state) {
        this.state = state;
    }
}
