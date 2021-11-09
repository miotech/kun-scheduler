package com.miotech.kun.monitor.sla.common.service;

import com.miotech.kun.monitor.facade.model.sla.TaskDefinitionNode;
import com.miotech.kun.monitor.facade.sla.SlaFacade;
import com.miotech.kun.monitor.sla.common.dao.SlaDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SlaService implements SlaFacade {

    @Autowired
    private SlaDao slaDao;

    @Override
    public TaskDefinitionNode findById(Long taskDefId) {
        return slaDao.findById(taskDefId);
    }

    @Override
    public void save(TaskDefinitionNode taskDefinitionNode) {
        slaDao.save(taskDefinitionNode);
    }

    @Override
    public void update(TaskDefinitionNode taskDefinitionNode) {
        slaDao.update(taskDefinitionNode);
    }

    @Override
    public void bind(Long from, Long to, TaskDefinitionNode.Relationship relationship) {
        slaDao.bind(from, to, relationship);
    }

    @Override
    public void unbind(Long from, Long to, TaskDefinitionNode.Relationship relationship) {
        slaDao.unbind(from, to, relationship);
    }

    @Override
    public void unbind(Long to, TaskDefinitionNode.Relationship relationship) {
        slaDao.unbind(to, relationship);
    }

    @Override
    public void deleteNodeAndRelationship(Long taskDefId) {
        if (taskDefId == null) {
            return;
        }

        slaDao.deleteNodeAndRelationship(taskDefId);
    }

    public void updateRunTime(Long definitionId, int runTime) {
        slaDao.updateRunTime(definitionId, runTime);
    }

    public List<List<TaskDefinitionNode>> findDownstreamPathHasSlaConfig(Long definitionId) {
        return slaDao.findDownstreamPathHasSlaConfig(definitionId);
    }

}
