package com.miotech.kun.workflow.common.lineage.node;

import com.miotech.kun.workflow.core.annotation.Internal;
import com.miotech.kun.workflow.core.model.task.Task;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@NodeEntity(label = "KUN_TASK")
public class TaskNode {
    public static TaskNode from(Task task) {
        TaskNode instance = new TaskNode();
        instance.taskId = task.getId();
        instance.taskName = task.getName();
        instance.description = task.getDescription();
        return instance;
    }

    /**
     * Neo4j-OGM requires a public no-args constructor to be able to construct objects from all annotated entities.
     */
    public TaskNode() {
    }

    public TaskNode(Long taskId) {
        this.taskId = taskId;
    }

    public TaskNode(Long taskId, String taskName) {
        this(taskId);
        this.taskName = taskName;
    }

    public TaskNode(Long taskId, String taskName, String description) {
        this(taskId, taskName);
        this.description = description;
    }

    @Id
    private Long taskId;

    private String taskName;

    private String description;

    @Relationship(type = "IS_INPUT_OF_KUN_TASK", direction = "INCOMING")
    private Set<DatasetNode> inlets = new LinkedHashSet<>();

    @Relationship(type = "OUTPUTS_TO_KUN_DATASET")
    private Set<DatasetNode> outlets = new LinkedHashSet<>();

    public void addInlet(DatasetNode datasetNode) {
        inlets.add(datasetNode);
    }

    public void removeInlet(DatasetNode datasetNode) {
        inlets.remove(datasetNode);
    }

    public void addOutlet(DatasetNode datasetNode) {
        outlets.add(datasetNode);
    }

    public void removeOutlet(DatasetNode datasetNode) {
        outlets.remove(datasetNode);
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get inlets of a task. Cannot guarantee that all lineage relations of this node is fetched properly.
     */
    @Internal
    public Set<DatasetNode> getInlets() {
        return this.inlets;
    }

    /**
     * Get outlets of a task. Cannot guarantee that all lineage relations of this node is fetched properly.
     */
    @Internal
    public Set<DatasetNode> getOutlets() {
        return this.outlets;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskNode taskNode = (TaskNode) o;
        return Objects.equals(taskId, taskNode.taskId) &&
                Objects.equals(taskName, taskNode.taskName) &&
                Objects.equals(description, taskNode.description) &&
                Objects.equals(inlets, taskNode.inlets) &&
                Objects.equals(outlets, taskNode.outlets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, taskName, description);
    }
}
