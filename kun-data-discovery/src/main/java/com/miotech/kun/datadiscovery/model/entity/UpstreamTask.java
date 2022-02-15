package com.miotech.kun.datadiscovery.model.entity;

public class UpstreamTask {

    private final Long id;

    private final String name;

    private final String description;

    private final Long definitionId;

    public UpstreamTask(Long id, String name, String description, Long definitionId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.definitionId = definitionId;
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

    public Long getDefinitionId() {
        return definitionId;
    }
}
