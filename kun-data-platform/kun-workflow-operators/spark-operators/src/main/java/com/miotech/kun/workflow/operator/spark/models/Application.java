package com.miotech.kun.workflow.operator.spark.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Application {
    String id;
    String user;
    String name;
    String state;
    String finalStatus;
    String amContainerLogs;

    public String getAmContainerLogs() {
                                     return amContainerLogs;
                                                            }

    public String getId() {
                        return id;
                                  }

    public void setId(String id) {
                               this.id = id;
                                            }

    public String getUser() {
                          return user;
                                      }

    public void setUser(String user) {
                                   this.user = user;
                                                    }

    public String getName() {
                          return name;
                                      }

    public void setName(String name) {
                                   this.name = name;
                                                    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    @SuppressWarnings("unchecked")
    @JsonProperty("app")
    private void unpackNested(Map<String, Object> app){
        this.id = (String) app.get("id");
        this.user = (String) app.get("user");
        this.name = (String) app.get("name");
        this.state = (String) app.get("state");
        this.finalStatus = (String) app.get("finalStatus");
        this.amContainerLogs = (String) app.get("amContainerLogs");
    }

}

