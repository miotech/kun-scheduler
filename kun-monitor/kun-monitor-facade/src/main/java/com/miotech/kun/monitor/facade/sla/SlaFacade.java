package com.miotech.kun.monitor.facade.sla;

import com.miotech.kun.monitor.facade.model.sla.TaskDefinitionNode;

import java.util.List;

public interface SlaFacade {

    /**
     * Query node in neo4j according to taskDefId
     * @param taskDefId TaskDefinition id
     * @return
     */
    TaskDefinitionNode findById(Long taskDefId);

    /**
     * Create node in neo4j
     * @param taskDefinitionNode
     */
    void save(TaskDefinitionNode taskDefinitionNode);

    /**
     * Update node in neo4j
     * @param taskDefinitionNode
     */
    void update(TaskDefinitionNode taskDefinitionNode);

    /**
     * Bind the relationship between two nodes
     * @param from
     * @param to
     * @param relationship
     */
    void bind(Long from, Long to, TaskDefinitionNode.Relationship relationship);

    /**
     * Unbind the relationship between two nodes
     * @param from
     * @param to
     * @param relationship
     */
    void unbind(Long from, Long to, TaskDefinitionNode.Relationship relationship);

    /**
     * Unbind all relationships on the node
     * @param to
     * @param relationship
     */
    void unbind(Long to, TaskDefinitionNode.Relationship relationship);

    /**
     * Delete node and its binding relationship
     * @param taskDefId
     */
    void deleteNodeAndRelationship(Long taskDefId);

    /**
     * Update the dependencies in the node
     * @param deployedTaskIds
     * @param taskDefId
     * @param name
     * @param level
     * @param deadline
     * @param workflowTaskId
     */
    void updateDependencies(List<Long> deployedTaskIds, Long taskDefId, String name, Integer level, Integer deadline, Long workflowTaskId);
}
