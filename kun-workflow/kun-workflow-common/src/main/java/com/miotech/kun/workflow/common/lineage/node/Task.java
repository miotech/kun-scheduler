package com.miotech.kun.workflow.common.lineage.node;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label = "KUN_TASK")
public class Task {
    /**
     * Neo4j-OGM requires a public no-args constructor to be able to construct objects from all annotated entities.
     */
    public Task() {
    }

    @Id
    private Long taskId;

    private Long ownerId;

    private String taskName;

    public Long getTaskId() {
        return taskId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getTaskName() {
        return taskName;
    }
}
