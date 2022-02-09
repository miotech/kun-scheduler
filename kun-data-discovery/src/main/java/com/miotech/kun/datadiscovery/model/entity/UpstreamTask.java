package com.miotech.kun.datadiscovery.model.entity;

public class UpstreamTask {

    private final Long id;

    private final String name;

    private final String description;

    public UpstreamTask(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
