package com.miotech.kun.workflow.common.lineage.node;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.kun.metadata.core.model.Dataset;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@JsonSerialize
@NodeEntity(label = "KUN_DATASET")
public class DatasetNode {
    public static DatasetNode from(Dataset dataset) {
        DatasetNode instance = new DatasetNode();
        instance.setGid(dataset.getGid());
        instance.setDatasetName(dataset.getName());
        return instance;
    }

    /**
     * Neo4j-OGM requires a public no-args constructor to be able to construct objects from all annotated entities.
     */
    public DatasetNode() {
    }

    public DatasetNode(Long gid, String datasetName) {
        this.gid = gid;
        this.datasetName = datasetName;
    }

    /**
     * Global id of dataset
     */
    @Id
    private Long gid;

    /**
     * Name of dataset
     */
    @Property(name="datasetName")
    private String datasetName;

    @Relationship(type = "IS_INPUT_OF_KUN_TASK")
    private Set<TaskNode> asInputs = new LinkedHashSet<>();

    @Relationship(type = "OUTPUTS_TO_KUN_DATASET", direction = "INCOMING")
    private Set<TaskNode> asOutputs = new LinkedHashSet<>();

    public Long getGid() {
        return gid;
    }

    public void setGid(Long gid) {
        this.gid = gid;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public Set<TaskNode> getDownstreamTasks() {
        return asInputs;
    }

    public Set<TaskNode> getUpstreamTasks() {
        return asOutputs;
    }

    public void setAsInputOfTask(TaskNode taskNode) {
        asInputs.add(taskNode);
    }

    public void setAsOutputOfTask(TaskNode taskNode) {
        asOutputs.add(taskNode);
    }

    public void removeAsInputOfTask(TaskNode taskNode) {
        asInputs.remove(taskNode);
    }

    public void removeAsOutputOfTask(TaskNode taskNode) {
        asOutputs.remove(taskNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetNode that = (DatasetNode) o;
        return Objects.equals(gid, that.gid) &&
                Objects.equals(datasetName, that.datasetName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gid, datasetName);
    }
}
