package com.miotech.kun.workflow.common.lineage.relation;

import org.neo4j.ogm.annotation.*;

@RelationshipEntity(type = "IS_INPUT_OF_KUN_TASK")
public class TaskInput {
    /**
     * Neo4j-OGM requires a public no-args constructor to be able to construct objects from all annotated entities.
     */
    public TaskInput() {
    }

    @Id
    @GeneratedValue
    private Long id;

    @StartNode
    private Dataset dataset;

    @EndNode
    private Task task;

    public Long getId() {
        return id;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public Task getTask() {
        return task;
    }
}
