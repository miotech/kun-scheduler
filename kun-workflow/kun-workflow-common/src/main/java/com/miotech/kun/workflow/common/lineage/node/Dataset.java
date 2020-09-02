package com.miotech.kun.workflow.common.lineage.node;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label = "KUN_DATASET")
public class Dataset {
    /**
     * Neo4j-OGM requires a public no-args constructor to be able to construct objects from all annotated entities.
     */
    public Dataset() {
    }

    /**
     * Global id of dataset
     */
    @Id
    private Long gid;

    /**
     * Name of dataset
     */
    private String datasetName;

    public Long getGid() {
        return gid;
    }

    public String getDatasetName() {
        return datasetName;
    }
}
