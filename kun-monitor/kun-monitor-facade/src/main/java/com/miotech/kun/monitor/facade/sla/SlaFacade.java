package com.miotech.kun.monitor.facade.sla;

import com.miotech.kun.monitor.facade.model.sla.TaskDefinitionNode;

public interface SlaFacade {

    TaskDefinitionNode findById(Long taskDefId);

    void save(TaskDefinitionNode taskDefinitionNode);

    void update(TaskDefinitionNode taskDefinitionNode);

    void bind(Long from, Long to, TaskDefinitionNode.Relationship relationship);

    void unbind(Long from, Long to, TaskDefinitionNode.Relationship relationship);

    void unbind(Long to, TaskDefinitionNode.Relationship relationship);

    void deleteNodeAndRelationship(Long taskDefId);

}
