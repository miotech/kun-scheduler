package com.miotech.kun.workflow.common.lineage.relation;

import com.miotech.kun.workflow.common.lineage.node.Dataset;
import com.miotech.kun.workflow.common.lineage.node.Task;
import org.neo4j.ogm.annotation.*;

@RelationshipEntity(type = "OUTPUTS_TO_KUN_DATASET")
public class TaskOutput {
    /**
     * Neo4j-OGM requires a public no-args constructor to be able to construct objects from all annotated entities.
     */
    public TaskOutput() {
    }

    @Id
    @GeneratedValue
    private Long id;

    @StartNode
    Task task;

    @EndNode
    Dataset dataset;
}
