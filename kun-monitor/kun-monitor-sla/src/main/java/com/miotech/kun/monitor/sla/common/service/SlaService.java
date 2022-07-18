package com.miotech.kun.monitor.sla.common.service;

import com.miotech.kun.dataplatform.facade.TaskDefinitionFacade;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.monitor.facade.model.sla.TaskDefinitionNode;
import com.miotech.kun.monitor.facade.sla.SlaFacade;
import com.miotech.kun.monitor.sla.common.dao.SlaDao;
import com.miotech.kun.monitor.sla.common.dao.TaskTimelineDao;
import com.miotech.kun.monitor.sla.model.BacktrackingTaskDefinition;
import com.miotech.kun.monitor.sla.model.SlaBacktrackingInformation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SlaService implements SlaFacade {

    @Autowired
    private SlaDao slaDao;

    @Autowired
    private TaskTimelineDao taskTimelineDao;

    @Autowired
    private TaskDefinitionFacade taskDefinitionFacade;

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

    @Override
    public void updateDependencies(List<Long> deployedTaskIds, Long taskDefId, String name, Integer level, Integer deadline, Long workflowTaskId) {
        TaskDefinitionNode node = findById(taskDefId);
        if (node == null) {
            save(TaskDefinitionNode.from(taskDefId, name, level, deadline, workflowTaskId, null));
        } else {
            update(TaskDefinitionNode.from(taskDefId, name, level,deadline , workflowTaskId, null));
        }

        unbind(taskDefId, TaskDefinitionNode.Relationship.OUTPUT);
        for (Long id : deployedTaskIds) {
            bind(id, taskDefId, TaskDefinitionNode.Relationship.OUTPUT);
        }
    }

    public void updateAvgTaskRunTimeLastSevenTimes(Long definitionId, int runTime) {
        slaDao.updateAvgTaskRunTimeLastSevenTimes(definitionId, runTime);
    }

    public List<List<TaskDefinitionNode>> findDownstreamPathHasSlaConfig(Long definitionId) {
        return slaDao.findDownstreamPathHasSlaConfig(definitionId);
    }

    public SlaBacktrackingInformation findBacktrackingInformation(Long taskDefinitionId) {
        TaskDefinitionNode taskDefinitionNode = findById(taskDefinitionId);
        Integer avgTaskRunTimeLastSevenTimes = taskDefinitionNode == null ? null : taskDefinitionNode.getAvgTaskRunTimeLastSevenTimes();

        BacktrackingTaskDefinition backtrackingTaskDefinition = taskTimelineDao.fetchBacktrackingByDefinitionId(taskDefinitionId);
        if (backtrackingTaskDefinition != null) {
            TaskDefinition taskDefinition = taskDefinitionFacade.find(backtrackingTaskDefinition.getDefinitionId());
            backtrackingTaskDefinition.setDefinitionName(taskDefinition.getName());
        }
        return SlaBacktrackingInformation.builder()
                .avgTaskRunTimeLastSevenTimes(avgTaskRunTimeLastSevenTimes)
                .backtrackingTaskDefinition(backtrackingTaskDefinition)
                .build();
    }
}
